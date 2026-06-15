package space.controlnet.mineagent.ae.common.terminal;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import space.controlnet.mineagent.core.session.TerminalBinding;

import java.util.Optional;
import java.util.function.Function;

final class AeTerminalContextResolution {
    interface TerminalLike {
        boolean isRemovedHost();
    }

    interface PartHostLike {
        @Nullable Object getPart(String sideName);
    }

    interface BindingLookupLike {
        boolean hasDimension(String dimensionId);

        @Nullable Object getBlockEntity(String dimensionId, int x, int y, int z);
    }

    record MenuState(boolean aiTerminalMenu, Optional<Object> host) {
        MenuState {
            host = host == null ? Optional.empty() : host;
        }
    }

    private AeTerminalContextResolution() {
    }

    static <T> Optional<T> fromMenuState(@Nullable MenuState state, Function<TerminalLike, T> contextFactory) {
        if (state == null || !state.aiTerminalMenu()) {
            return Optional.empty();
        }
        return resolveTerminal(state.host().orElse(null)).map(contextFactory);
    }

    static <T> Optional<T> fromBindingLookup(
            @Nullable BindingLookupLike lookup,
            @Nullable TerminalBinding binding,
            Function<TerminalLike, T> contextFactory
    ) {
        if (lookup == null || binding == null) {
            return Optional.empty();
        }

        String dimensionId;
        try {
            dimensionId = new ResourceLocation(binding.dimensionId()).toString();
        } catch (Exception ignored) {
            return Optional.empty();
        }

        if (!lookup.hasDimension(dimensionId)) {
            return Optional.empty();
        }

        Object blockEntity = lookup.getBlockEntity(dimensionId, binding.x(), binding.y(), binding.z());
        if (blockEntity == null) {
            return Optional.empty();
        }

        return resolveBindingTerminal(blockEntity, binding.side()).map(contextFactory);
    }

    private static Optional<TerminalLike> resolveBindingTerminal(Object blockEntity, Optional<String> side) {
        Optional<String> normalizedSide = side == null ? Optional.empty() : side;
        if (normalizedSide.isEmpty()) {
            return resolveTerminal(blockEntity);
        }

        if (!(blockEntity instanceof PartHostLike partHost)) {
            return Optional.empty();
        }

        Direction direction;
        try {
            direction = Direction.valueOf(normalizedSide.get());
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }

        return resolveTerminal(partHost.getPart(direction.name()));
    }

    private static Optional<TerminalLike> resolveTerminal(@Nullable Object candidate) {
        if (!(candidate instanceof TerminalLike terminal) || terminal.isRemovedHost()) {
            return Optional.empty();
        }
        return Optional.of(terminal);
    }
}
