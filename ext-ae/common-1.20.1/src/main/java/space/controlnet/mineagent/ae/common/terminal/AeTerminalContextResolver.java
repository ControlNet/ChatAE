package space.controlnet.mineagent.ae.common.terminal;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import space.controlnet.mineagent.common.menu.AiTerminalMenu;
import space.controlnet.mineagent.common.terminal.TerminalContextResolver;
import space.controlnet.mineagent.core.session.TerminalBinding;
import space.controlnet.mineagent.ae.core.terminal.AiTerminalData;
import space.controlnet.mineagent.ae.core.terminal.AeTerminalContext;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

public final class AeTerminalContextResolver implements TerminalContextResolver {
    @Override
    public Optional<space.controlnet.mineagent.core.terminal.TerminalContext> fromPlayer(ServerPlayer player) {
        return fromPlayerMenuState(
                createMenuState(player),
                terminal -> new PlayerTerminalContext(player, requireTerminal(terminal))
        );
    }

    @Override
    public Optional<space.controlnet.mineagent.core.terminal.TerminalContext> fromPlayerAtBinding(ServerPlayer player, TerminalBinding binding) {
        return fromPlayerBindingLookup(
                createBindingLookup(player),
                binding,
                terminal -> new PlayerTerminalContext(player, requireTerminal(terminal))
        );
    }

    static <T> Optional<T> fromPlayerMenuState(
            @Nullable AeTerminalContextResolution.MenuState state,
            Function<AeTerminalContextResolution.TerminalLike, T> contextFactory
    ) {
        return AeTerminalContextResolution.fromMenuState(state, contextFactory);
    }

    static <T> Optional<T> fromPlayerBindingLookup(
            @Nullable AeTerminalContextResolution.BindingLookupLike lookup,
            @Nullable TerminalBinding binding,
            Function<AeTerminalContextResolution.TerminalLike, T> contextFactory
    ) {
        return AeTerminalContextResolution.fromBindingLookup(lookup, binding, contextFactory);
    }

    private static AeTerminalContextResolution.MenuState createMenuState(ServerPlayer player) {
        if (player == null) {
            return null;
        }
        if (!(player.containerMenu instanceof AiTerminalMenu menu)) {
            return new AeTerminalContextResolution.MenuState(false, Optional.empty());
        }
        return new AeTerminalContextResolution.MenuState(true, menu.getHost().map(AeTerminalContextResolver::adaptMenuHost));
    }

    private static AeTerminalContextResolution.BindingLookupLike createBindingLookup(ServerPlayer player) {
        if (player == null || player.getServer() == null) {
            return null;
        }
        return new ServerBindingLookup(player.getServer());
    }

    private static Object adaptMenuHost(Object host) {
        if (host instanceof AeTerminalHost terminal) {
            return new TerminalAdapter(terminal);
        }
        return host;
    }

    private static Object adaptBindingTarget(BlockEntity blockEntity) {
        if (blockEntity instanceof AeTerminalHost terminal && blockEntity instanceof IPartHost partHost) {
            return new TerminalPartHostAdapter(terminal, partHost);
        }
        if (blockEntity instanceof AeTerminalHost terminal) {
            return new TerminalAdapter(terminal);
        }
        if (blockEntity instanceof IPartHost partHost) {
            return new PartHostAdapter(partHost);
        }
        return blockEntity;
    }

    private static Object adaptPart(IPart part) {
        if (part instanceof AeTerminalHost terminal) {
            return new TerminalAdapter(terminal);
        }
        return part;
    }

    private static AeTerminalHost requireTerminal(AeTerminalContextResolution.TerminalLike terminal) {
        if (terminal instanceof TerminalAdapter adapter) {
            return adapter.terminal();
        }
        if (terminal instanceof TerminalPartHostAdapter adapter) {
            return adapter.terminal();
        }
        throw new IllegalStateException("Unexpected terminal adapter: " + terminal.getClass().getName());
    }

    private record TerminalAdapter(AeTerminalHost terminal) implements AeTerminalContextResolution.TerminalLike {
        @Override
        public boolean isRemovedHost() {
            return terminal.isRemovedHost();
        }
    }

    private record PartHostAdapter(IPartHost partHost) implements AeTerminalContextResolution.PartHostLike {
        @Override
        public Object getPart(String sideName) {
            return adaptPart(partHost.getPart(Direction.valueOf(sideName)));
        }
    }

    private record TerminalPartHostAdapter(AeTerminalHost terminal, IPartHost partHost)
            implements AeTerminalContextResolution.TerminalLike, AeTerminalContextResolution.PartHostLike {
        @Override
        public boolean isRemovedHost() {
            return terminal.isRemovedHost();
        }

        @Override
        public Object getPart(String sideName) {
            return adaptPart(partHost.getPart(Direction.valueOf(sideName)));
        }
    }

    private record ServerBindingLookup(net.minecraft.server.MinecraftServer server)
            implements AeTerminalContextResolution.BindingLookupLike {
        @Override
        public boolean hasDimension(String dimensionId) {
            return resolveLevel(dimensionId) != null;
        }

        @Override
        public Object getBlockEntity(String dimensionId, int x, int y, int z) {
            ServerLevel level = resolveLevel(dimensionId);
            if (level == null) {
                return null;
            }
            BlockEntity blockEntity = level.getBlockEntity(new BlockPos(x, y, z));
            if (blockEntity == null) {
                return null;
            }
            return adaptBindingTarget(blockEntity);
        }

        private ServerLevel resolveLevel(String dimensionId) {
            ResourceLocation id = new ResourceLocation(dimensionId);
            ResourceKey<net.minecraft.world.level.Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, id);
            return server.getLevel(dimensionKey);
        }
    }

    private static final class PlayerTerminalContext implements AeTerminalContext {
        private final ServerPlayer player;
        private final AeTerminalHost terminal;

        private PlayerTerminalContext(ServerPlayer player, AeTerminalHost terminal) {
            this.player = player;
            this.terminal = terminal;
        }

        @Override
        public AiTerminalData.AeListResult listItems(String query, boolean craftableOnly, int limit, String pageToken) {
            return terminal.listItems(query, craftableOnly, limit, pageToken);
        }

        @Override
        public AiTerminalData.AeListResult listCraftables(String query, int limit, String pageToken) {
            return terminal.listCraftables(query, limit, pageToken);
        }

        @Override
        public AiTerminalData.AeCraftSimulation simulateCraft(String itemId, long count) {
            return terminal.simulateCraft(player, itemId, count);
        }

        @Override
        public AiTerminalData.AeCraftRequest requestCraft(String itemId, long count, String cpuName) {
            return terminal.requestCraft(player, itemId, count, cpuName);
        }

        @Override
        public AiTerminalData.AeJobStatus jobStatus(String jobId) {
            return terminal.jobStatus(jobId);
        }

        @Override
        public AiTerminalData.AeJobStatus cancelJob(String jobId) {
            return terminal.cancelJob(jobId);
        }
    }
}
