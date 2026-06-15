package space.controlnet.mineagent.ae.common.part;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingSimulationRequester;
import appeng.api.networking.crafting.ICraftingSubmitResult;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import space.controlnet.mineagent.ae.core.terminal.AiTerminalData;
import space.controlnet.mineagent.ae.common.terminal.AeTerminalHost;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class AiTerminalPartOperations {
    private final AiTerminalPartOperationsJobLifecycle<ICraftingLink> jobLifecycle =
            new AiTerminalPartOperationsJobLifecycle<>(ICraftingLink::isCanceled, ICraftingLink::isDone, ICraftingLink::cancel);

    public AiTerminalData.AeListResult listItems(IGrid grid, String query, boolean craftableOnly, int limit, @Nullable String pageToken) {
        var inv = grid.getStorageService().getCachedInventory();
        var craftables = grid.getCraftingService().getCraftables(AEItemKey.filter());

        List<AiTerminalData.AeEntry> entries = inv.keySet().stream()
                .filter(AEItemKey::is)
                .map(k -> (AEItemKey) k)
                .map(key -> new AiTerminalData.AeEntry(key.getId().toString(), inv.get(key), craftables.contains(key)))
                .toList();

        return AiTerminalPartOperationsListPagination.paginate(entries, query, craftableOnly, limit, pageToken);
    }

    public AiTerminalData.AeListResult listCraftables(IGrid grid, String query, int limit, @Nullable String pageToken) {
        List<AiTerminalData.AeEntry> entries = grid.getCraftingService().getCraftables(AEItemKey.filter()).stream()
                .filter(AEItemKey::is)
                .map(k -> (AEItemKey) k)
                .map(key -> new AiTerminalData.AeEntry(key.getId().toString(), 0, true))
                .toList();

        return AiTerminalPartOperationsListPagination.paginate(entries, query, false, limit, pageToken);
    }

    public AiTerminalData.AeCraftSimulation simulateCraft(Player player, IGrid grid, @Nullable Level level, AeTerminalHost host, String itemId, long count) {
        AEItemKey key = resolveKey(itemId);
        if (key == null) {
            return new AiTerminalData.AeCraftSimulation("", "error", List.of(), Optional.of("Unknown item: " + itemId));
        }

        String jobId = UUID.randomUUID().toString();
        AiTerminalPartOperationsJobLifecycle.CraftJob<ICraftingLink> job = jobLifecycle.startCalculatingJob(jobId);

        ICraftingSimulationRequester requester = new SimulationRequester(player, host);
        CompletableFuture<ICraftingPlan> future = toCompletable(grid.getCraftingService().beginCraftingCalculation(level, requester, key, count, CalculationStrategy.REPORT_MISSING_ITEMS));
        future.whenComplete((plan, error) -> {
            if (error != null) {
                jobLifecycle.putJob(jobId, job.withError("Crafting simulation failed: " + error.getMessage()));
                return;
            }
            if (plan == null) {
                jobLifecycle.putJob(jobId, job.withError("Crafting simulation failed"));
                return;
            }
            List<AiTerminalData.AePlanItem> missing = toPlanItems(plan.missingItems());
            jobLifecycle.putJob(jobId, job.withPlan(plan.simulation(), missing));
        });

        return new AiTerminalData.AeCraftSimulation(jobId, job.state().name().toLowerCase(Locale.ROOT), job.missingItems(), Optional.empty());
    }

    public AiTerminalData.AeCraftRequest requestCraft(Player player, IGrid grid, @Nullable Level level, AeTerminalHost host, String itemId, long count, @Nullable String cpuName) {
        AEItemKey key = resolveKey(itemId);
        if (key == null) {
            return new AiTerminalData.AeCraftRequest("", "error", Optional.of("Unknown item: " + itemId));
        }

        String jobId = UUID.randomUUID().toString();
        AiTerminalPartOperationsJobLifecycle.CraftJob<ICraftingLink> job = jobLifecycle.startCalculatingJob(jobId);

        ICraftingSimulationRequester requester = new SimulationRequester(player, host);
        CompletableFuture<ICraftingPlan> future = toCompletable(grid.getCraftingService().beginCraftingCalculation(level, requester, key, count, CalculationStrategy.REPORT_MISSING_ITEMS));
        future.whenComplete((plan, error) -> {
            if (error != null) {
                jobLifecycle.putJob(jobId, job.withError("Crafting calculation failed: " + error.getMessage()));
                return;
            }
            if (plan == null) {
                jobLifecycle.putJob(jobId, job.withError("Crafting calculation failed"));
                return;
            }

            List<AiTerminalData.AePlanItem> missing = toPlanItems(plan.missingItems());
            if (plan.simulation()) {
                jobLifecycle.putJob(jobId, job.withPlan(true, missing));
                return;
            }

            if (level == null || level.isClientSide()) {
                jobLifecycle.putJob(jobId, job.withError("No server level"));
                return;
            }

            jobLifecycle.putJob(jobId, job.withMissingItems(missing));

            level.getServer().execute(() -> {
                AiTerminalPartOperationsJobLifecycle.CraftJob<ICraftingLink> current = jobLifecycle.getJobOrDefault(jobId, job);
                ICraftingCPU target = selectCpu(grid.getCraftingService().getCpus(), cpuName);
                if (cpuName != null && !cpuName.isBlank() && target == null) {
                    jobLifecycle.putJob(jobId, current.withError("CPU unavailable"));
                    return;
                }
                IActionSource actionSource = IActionSource.ofPlayer(player, host);
                ICraftingSubmitResult submit = grid.getCraftingService().submitJob(plan, host, target, false, actionSource);
                if (!submit.successful()) {
                    jobLifecycle.putJob(jobId, current.withError("Crafting submit failed: " + submit.errorCode()));
                    return;
                }

                ICraftingLink link = submit.link();
                if (link == null) {
                    jobLifecycle.putJob(jobId, current.withError("Crafting link unavailable"));
                    return;
                }

                jobLifecycle.putJob(jobId, current.withLink(link));
            });
        });

        return new AiTerminalData.AeCraftRequest(jobId, job.state().name().toLowerCase(Locale.ROOT), Optional.empty());
    }

    public AiTerminalData.AeJobStatus jobStatus(String jobId) {
        return jobLifecycle.jobStatus(jobId);
    }

    public AiTerminalData.AeJobStatus cancelJob(String jobId) {
        return jobLifecycle.cancelJob(jobId);
    }

    public com.google.common.collect.ImmutableSet<ICraftingLink> getRequestedJobs() {
        return com.google.common.collect.ImmutableSet.copyOf(jobLifecycle.getRequestedJobs());
    }

    public long insertCraftedItems(IGrid grid, ICraftingLink link, AEKey what, long amount, Actionable mode, AeTerminalHost host) {
        return grid.getStorageService().getInventory().insert(what, amount, mode, IActionSource.ofMachine(host));
    }

    public void jobStateChange(ICraftingLink link) {
        jobLifecycle.jobStateChange(link);
    }

    public void clearJobs() {
        jobLifecycle.clearJobs();
    }

    private static AEItemKey resolveKey(String itemId) {
        ResourceLocation id = AiTerminalPartOperationsJobLifecycle.resolveKey(
                itemId,
                resourceLocation -> net.minecraft.core.registries.BuiltInRegistries.ITEM.getOptional(resourceLocation).isPresent()
        );
        if (id == null) {
            return null;
        }
        return AEItemKey.of(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(id));
    }

    private static CompletableFuture<ICraftingPlan> toCompletable(java.util.concurrent.Future<ICraftingPlan> future) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return future.get(30000, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                throw new RuntimeException("Crafting calculation timed out", e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static ICraftingCPU selectCpu(Set<ICraftingCPU> cpus, @Nullable String cpuName) {
        return AiTerminalPartOperationsJobLifecycle.selectCpu(cpus, cpu -> {
            var name = cpu.getName();
            return name == null ? null : name.getString();
        }, cpuName);
    }

    private static List<AiTerminalData.AePlanItem> toPlanItems(KeyCounter counter) {
        if (counter == null || counter.isEmpty()) {
            return List.of();
        }
        List<AiTerminalData.AePlanItem> out = new ArrayList<>();
        for (var entry : counter) {
            AEKey key = entry.getKey();
            long amount = entry.getLongValue();
            if (key instanceof AEItemKey itemKey) {
                out.add(new AiTerminalData.AePlanItem(itemKey.getId().toString(), amount));
            }
        }
        return out;
    }

    private record SimulationRequester(Player player, AeTerminalHost host) implements ICraftingSimulationRequester {
        @Override
        public IActionSource getActionSource() {
            return IActionSource.ofPlayer(player, host);
        }
    }
}
