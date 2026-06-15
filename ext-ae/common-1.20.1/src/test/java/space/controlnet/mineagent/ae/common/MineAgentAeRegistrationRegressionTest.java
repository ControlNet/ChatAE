package space.controlnet.mineagent.ae.common;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import space.controlnet.mineagent.ae.common.terminal.AeTerminalContextResolver;
import space.controlnet.mineagent.common.client.render.ToolOutputRendererRegistry;
import space.controlnet.mineagent.common.terminal.TerminalContextRegistry;
import space.controlnet.mineagent.common.tools.ToolRegistry;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public final class MineAgentAeRegistrationRegressionTest {
    @BeforeEach
    void setUp() {
        resetRegistries();
    }

    @AfterEach
    void tearDown() {
        resetRegistries();
    }

    @Test
    void task10_mineAgentAe_commonWiring_registersAeProviderResolverRendererAndGroupId() {
        assertEmpty("task10/pre/provider-id", ToolRegistry.getProviderId("ae"));
        assertEmpty("task10/pre/group-id", ToolRegistry.getGroupId("ae.list_items"));
        assertNull("task10/pre/renderer", ToolOutputRendererRegistry.tryRender(json("{\"jobId\":\"job-7\",\"status\":\"queued\"}")));
        assertNull("task10/pre/resolver", getRegisteredResolver());

        MineAgentAe.initCommonWiring();

        assertEquals("task10/provider-id", "ae", requirePresent("task10/provider-id", ToolRegistry.getProviderId("ae.list_items")));
        assertEquals("task10/group-id", "mineagentae", requirePresent("task10/group-id", ToolRegistry.getGroupId("ae.list_items")));
        assertNotNull("task10/tool-spec", ToolRegistry.getToolSpec("ae.list_items"));
        assertEquals("task10/tool-count", 6, ToolRegistry.getToolSpecs().size());

        assertTrue("task10/resolver-type", getRegisteredResolver() instanceof AeTerminalContextResolver);
        assertEquals("task10/renderer-lines", List.of("Job job-7 — queued"),
                ToolOutputRendererRegistry.tryRender(json("{\"jobId\":\"job-7\",\"status\":\"queued\"}")));
    }

    @Test
    void task10_mineAgentAe_sourceStillDelegatesPartRegistriesAndModels() {
        String mineAgentAeSource = readSource("ext-ae/common-1.20.1/src/main/java/space/controlnet/mineagent/ae/common/MineAgentAe.java");
        String partRegistriesSource = readSource("ext-ae/common-1.20.1/src/main/java/space/controlnet/mineagent/ae/common/part/MineAgentAePartRegistries.java");

        assertTrue("task10/source/init-calls-common-wiring", mineAgentAeSource.contains("initCommonWiring();"));
        assertTrue("task10/source/init-calls-part-registries", mineAgentAeSource.contains("MineAgentAePartRegistries.init();"));
        assertTrue("task10/source/provider-registration", mineAgentAeSource.contains("ToolRegistry.register(\"ae\", new AeToolProvider())"));
        assertTrue("task10/source/group-registration", mineAgentAeSource.contains("ToolRegistry.setGroupId(\"ae\", MOD_ID)"));
        assertTrue("task10/source/resolver-registration", mineAgentAeSource.contains("TerminalContextRegistry.register(new AeTerminalContextResolver())"));
        assertTrue("task10/source/renderer-registration", mineAgentAeSource.contains("ToolOutputRendererRegistry.register(new AeToolOutputRenderer())"));
        assertTrue("task10/source/part-item-registration", partRegistriesSource.contains("AI_TERMINAL_PART_ITEM"));
        assertTrue("task10/source/part-tab-registration", partRegistriesSource.contains("MAIN_TAB"));
        assertTrue("task10/source/model-registration", partRegistriesSource.contains("PartModels.registerModels("));
        assertTrue("task10/source/model-off", partRegistriesSource.contains("AiTerminalPartModelIds.MODEL_OFF"));
        assertTrue("task10/source/model-on", partRegistriesSource.contains("AiTerminalPartModelIds.MODEL_ON"));
    }

    private static void resetRegistries() {
        ToolRegistry.unregister("ae");
        ToolOutputRendererRegistry.clear();
        TerminalContextRegistry.register(null);
    }

    private static AeTerminalContextResolver getRegisteredResolver() {
        try {
            Field field = TerminalContextRegistry.class.getDeclaredField("RESOLVER");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            AtomicReference<Object> resolverRef = (AtomicReference<Object>) field.get(null);
            Object resolver = resolverRef == null ? null : resolverRef.get();
            return resolver instanceof AeTerminalContextResolver aeResolver ? aeResolver : null;
        } catch (Exception exception) {
            throw new AssertionError("task10/resolver-reflection", exception);
        }
    }

    private static JsonObject json(String raw) {
        return JsonParser.parseString(raw).getAsJsonObject();
    }

    private static String readSource(String relativePath) {
        try {
            Path cwd = Path.of(System.getProperty("user.dir"));
            for (Path candidate : List.of(
                    cwd.resolve(relativePath),
                    cwd.resolve("..").resolve(relativePath).normalize(),
                    cwd.resolve("../..").resolve(relativePath).normalize()
            )) {
                if (Files.exists(candidate)) {
                    return Files.readString(candidate, StandardCharsets.UTF_8);
                }
            }
            throw new java.nio.file.NoSuchFileException(relativePath);
        } catch (Exception exception) {
            throw new AssertionError("task10/source-read/" + relativePath, exception);
        }
    }

    private static <T> T requirePresent(String assertionName, Optional<T> actual) {
        if (actual.isPresent()) {
            return actual.get();
        }
        throw new AssertionError(assertionName + " -> expected Optional to be present, actual: empty");
    }

    private static void assertEquals(String assertionName, Object expected, Object actual) {
        if ((expected == null && actual == null) || (expected != null && expected.equals(actual))) {
            return;
        }
        throw new AssertionError(assertionName + " -> expected: " + expected + ", actual: " + actual);
    }

    private static void assertNotNull(String assertionName, Object actual) {
        if (actual != null) {
            return;
        }
        throw new AssertionError(assertionName + " -> expected non-null");
    }

    private static void assertNull(String assertionName, Object actual) {
        if (actual == null) {
            return;
        }
        throw new AssertionError(assertionName + " -> expected null, actual: " + actual);
    }

    private static void assertTrue(String assertionName, boolean condition) {
        if (condition) {
            return;
        }
        throw new AssertionError(assertionName + " -> expected true");
    }

    private static void assertEmpty(String assertionName, Optional<?> actual) {
        if (actual.isEmpty()) {
            return;
        }
        throw new AssertionError(assertionName + " -> expected Optional to be empty, actual: " + actual.get());
    }
}
