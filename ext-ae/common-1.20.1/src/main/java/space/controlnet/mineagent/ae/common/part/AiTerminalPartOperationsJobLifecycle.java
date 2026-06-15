package space.controlnet.mineagent.ae.common.part;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import space.controlnet.mineagent.ae.core.terminal.AiTerminalData;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

final class AiTerminalPartOperationsJobLifecycle<T> {
    private final Map<String, CraftJob<T>> jobs = new ConcurrentHashMap<>();
    private final Predicate<T> linkCanceled;
    private final Predicate<T> linkDone;
    private final Consumer<T> linkCancel;

    AiTerminalPartOperationsJobLifecycle(Predicate<T> linkCanceled, Predicate<T> linkDone, Consumer<T> linkCancel) {
        this.linkCanceled = Objects.requireNonNull(linkCanceled, "linkCanceled");
        this.linkDone = Objects.requireNonNull(linkDone, "linkDone");
        this.linkCancel = Objects.requireNonNull(linkCancel, "linkCancel");
    }

    CraftJob<T> startCalculatingJob(String jobId) {
        CraftJob<T> job = new CraftJob<>(jobId, CraftJobState.CALCULATING, null, List.of(), Optional.empty());
        jobs.put(jobId, job);
        return job;
    }

    void putJob(String jobId, CraftJob<T> job) {
        jobs.put(jobId, job);
    }

    CraftJob<T> getJobOrDefault(String jobId, CraftJob<T> fallback) {
        return jobs.getOrDefault(jobId, fallback);
    }

    AiTerminalData.AeJobStatus jobStatus(String jobId) {
        CraftJob<T> job = jobs.get(jobId);
        if (job == null) {
            return new AiTerminalData.AeJobStatus(jobId, "unknown", List.of(), Optional.of("Job not found"));
        }

        job = refreshJob(job);
        jobs.put(jobId, job);
        return toStatus(job);
    }

    AiTerminalData.AeJobStatus cancelJob(String jobId) {
        CraftJob<T> job = jobs.get(jobId);
        if (job == null) {
            return new AiTerminalData.AeJobStatus(jobId, "unknown", List.of(), Optional.of("Job not found"));
        }

        if (job.link() != null) {
            linkCancel.accept(job.link());
        }

        job = job.withState(CraftJobState.CANCELED);
        jobs.put(jobId, job);
        return toStatus(job);
    }

    Set<T> getRequestedJobs() {
        return Set.copyOf(jobs.values().stream()
                .map(CraftJob::link)
                .filter(Objects::nonNull)
                .filter(link -> !linkCanceled.test(link) && !linkDone.test(link))
                .toList());
    }

    void jobStateChange(T link) {
        jobs.values().stream()
                .filter(job -> Objects.equals(link, job.link()))
                .findFirst()
                .ifPresent(job -> jobs.put(job.jobId(), job.withState(linkDone.test(link) ? CraftJobState.DONE : CraftJobState.CANCELED)));
    }

    void clearJobs() {
        jobs.clear();
    }

    static @Nullable ResourceLocation resolveKey(@Nullable String itemId, Predicate<ResourceLocation> isAvailable) {
        if (itemId == null || itemId.isBlank()) {
            return null;
        }
        ResourceLocation id = ResourceLocation.tryParse(itemId);
        if (id == null || !isAvailable.test(id)) {
            return null;
        }
        return id;
    }

    static <T> @Nullable T selectCpu(Set<T> cpus, Function<T, String> nameResolver, @Nullable String cpuName) {
        if (cpuName == null || cpuName.isBlank()) {
            return null;
        }
        String target = cpuName.toLowerCase(Locale.ROOT);
        for (T cpu : cpus) {
            String name = nameResolver.apply(cpu);
            if (name != null && name.toLowerCase(Locale.ROOT).contains(target)) {
                return cpu;
            }
        }
        return null;
    }

    CraftJob<T> refreshJob(CraftJob<T> job) {
        if (job.state() == CraftJobState.CALCULATING) {
            return job;
        }
        T link = job.link();
        if (link == null) {
            return job;
        }
        if (linkCanceled.test(link)) {
            return job.withState(CraftJobState.CANCELED);
        }
        if (linkDone.test(link)) {
            return job.withState(CraftJobState.DONE);
        }
        return job;
    }

    private static <T> AiTerminalData.AeJobStatus toStatus(CraftJob<T> job) {
        return new AiTerminalData.AeJobStatus(
                job.jobId(),
                job.state().name().toLowerCase(Locale.ROOT),
                job.missingItems(),
                job.error()
        );
    }

    enum CraftJobState {
        CALCULATING,
        SUBMITTED,
        DONE,
        CANCELED,
        FAILED
    }

    record CraftJob<T>(String jobId, CraftJobState state, @Nullable T link, List<AiTerminalData.AePlanItem> missingItems, Optional<String> error) {
        CraftJob {
            missingItems = missingItems == null ? List.of() : List.copyOf(missingItems);
            error = error == null ? Optional.empty() : error;
        }

        CraftJob<T> withPlan(boolean simulation, List<AiTerminalData.AePlanItem> missing) {
            return new CraftJob<>(
                    jobId,
                    simulation ? CraftJobState.FAILED : CraftJobState.SUBMITTED,
                    link,
                    missing,
                    simulation
                            ? Optional.of("Missing items: " + missing.stream().map(AiTerminalData.AePlanItem::itemId).collect(Collectors.joining(", ")))
                            : Optional.empty()
            );
        }

        CraftJob<T> withLink(T link) {
            return new CraftJob<>(jobId, CraftJobState.SUBMITTED, link, missingItems, error);
        }

        CraftJob<T> withMissingItems(List<AiTerminalData.AePlanItem> missing) {
            return new CraftJob<>(jobId, state, link, missing, error);
        }

        CraftJob<T> withError(String message) {
            return new CraftJob<>(jobId, CraftJobState.FAILED, link, missingItems, Optional.ofNullable(message));
        }

        CraftJob<T> withState(CraftJobState state) {
            return new CraftJob<>(jobId, state, link, missingItems, error);
        }
    }
}
