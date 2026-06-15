package space.controlnet.mineagent.ae.common.terminal;

import org.junit.jupiter.api.Test;
import space.controlnet.mineagent.core.session.TerminalBinding;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class AeTerminalContextResolverRegressionTest {
    @Test
    void task8_fromPlayer_liveAeTerminalMenu_resolvesPresentContext() {
        Optional<String> resolved = AeTerminalContextResolver.fromPlayerMenuState(
                new AeTerminalContextResolution.MenuState(true, Optional.of(new FakeTerminal(false))),
                terminal -> "resolved"
        );

        assertPresent("task8/from-player/live-menu", resolved);
    }

    @Test
    void task8_fromPlayer_nullWrongMenuMissingHostWrongHostAndRemovedHost_resolveEmpty() {
        assertEmpty(
                "task8/from-player/null-player",
                AeTerminalContextResolver.fromPlayerMenuState(null, terminal -> "unexpected")
        );
        assertEmpty(
                "task8/from-player/wrong-menu-type",
                AeTerminalContextResolver.fromPlayerMenuState(
                        new AeTerminalContextResolution.MenuState(false, Optional.of(new FakeTerminal(false))),
                        terminal -> "unexpected"
                )
        );
        assertEmpty(
                "task8/from-player/missing-host",
                AeTerminalContextResolver.fromPlayerMenuState(
                        new AeTerminalContextResolution.MenuState(true, Optional.empty()),
                        terminal -> "unexpected"
                )
        );
        assertEmpty(
                "task8/from-player/wrong-host-type",
                AeTerminalContextResolver.fromPlayerMenuState(
                        new AeTerminalContextResolution.MenuState(true, Optional.of(new Object())),
                        terminal -> "unexpected"
                )
        );
        assertEmpty(
                "task8/from-player/removed-host",
                AeTerminalContextResolver.fromPlayerMenuState(
                        new AeTerminalContextResolution.MenuState(true, Optional.of(new FakeTerminal(true))),
                        terminal -> "unexpected"
                )
        );
    }

    @Test
    void task8_fromPlayerAtBinding_successWrongSideInvalidDimensionMissingBlockEntityAndRemovedHost_areDeterministic() {
        FakeBindingLookup successLookup = new FakeBindingLookup().withBlockEntity(
                "minecraft:overworld",
                1,
                2,
                3,
                new FakePartHost(Map.of("NORTH", new FakeTerminal(false)))
        );

        Optional<String> success = AeTerminalContextResolver.fromPlayerBindingLookup(
                successLookup,
                new TerminalBinding("minecraft:overworld", 1, 2, 3, Optional.of("NORTH")),
                terminal -> "resolved"
        );
        Optional<String> wrongSide = AeTerminalContextResolver.fromPlayerBindingLookup(
                successLookup,
                new TerminalBinding("minecraft:overworld", 1, 2, 3, Optional.of("SOUTH")),
                terminal -> "unexpected"
        );
        Optional<String> invalidDimension = AeTerminalContextResolver.fromPlayerBindingLookup(
                successLookup,
                new TerminalBinding("not a dimension", 1, 2, 3, Optional.of("NORTH")),
                terminal -> "unexpected"
        );
        Optional<String> missingBlockEntity = AeTerminalContextResolver.fromPlayerBindingLookup(
                new FakeBindingLookup().withDimension("minecraft:overworld"),
                new TerminalBinding("minecraft:overworld", 1, 2, 3, Optional.of("NORTH")),
                terminal -> "unexpected"
        );
        Optional<String> removedHost = AeTerminalContextResolver.fromPlayerBindingLookup(
                new FakeBindingLookup().withBlockEntity(
                        "minecraft:overworld",
                        1,
                        2,
                        3,
                        new FakePartHost(Map.of("NORTH", new FakeTerminal(true)))
                ),
                new TerminalBinding("minecraft:overworld", 1, 2, 3, Optional.of("NORTH")),
                terminal -> "unexpected"
        );

        assertPresent("task8/from-binding/success", success);
        assertEmpty("task8/from-binding/wrong-side", wrongSide);
        assertEmpty("task8/from-binding/invalid-dimension", invalidDimension);
        assertEmpty("task8/from-binding/missing-block-entity", missingBlockEntity);
        assertEmpty("task8/from-binding/removed-host", removedHost);
    }

    private static void assertPresent(String assertionName, Optional<?> actual) {
        if (actual.isPresent()) {
            return;
        }
        throw new AssertionError(assertionName + " -> expected Optional to be present, actual: empty");
    }

    private static void assertEmpty(String assertionName, Optional<?> actual) {
        if (actual.isEmpty()) {
            return;
        }
        throw new AssertionError(assertionName + " -> expected Optional to be empty, actual: " + actual.get());
    }

    private record FakeTerminal(boolean removedHost) implements AeTerminalContextResolution.TerminalLike {
        @Override
        public boolean isRemovedHost() {
            return removedHost;
        }
    }

    private record FakePartHost(Map<String, Object> parts) implements AeTerminalContextResolution.PartHostLike {
        private FakePartHost {
            parts = Map.copyOf(parts);
        }

        @Override
        public Object getPart(String sideName) {
            return parts.get(sideName);
        }
    }

    private static final class FakeBindingLookup implements AeTerminalContextResolution.BindingLookupLike {
        private final Set<String> dimensions = new LinkedHashSet<>();
        private final Map<BindingKey, Object> blockEntities = new LinkedHashMap<>();

        private FakeBindingLookup withDimension(String dimensionId) {
            dimensions.add(dimensionId);
            return this;
        }

        private FakeBindingLookup withBlockEntity(String dimensionId, int x, int y, int z, Object blockEntity) {
            dimensions.add(dimensionId);
            blockEntities.put(new BindingKey(dimensionId, x, y, z), blockEntity);
            return this;
        }

        @Override
        public boolean hasDimension(String dimensionId) {
            return dimensions.contains(dimensionId);
        }

        @Override
        public Object getBlockEntity(String dimensionId, int x, int y, int z) {
            return blockEntities.get(new BindingKey(dimensionId, x, y, z));
        }
    }

    private record BindingKey(String dimensionId, int x, int y, int z) {
    }
}
