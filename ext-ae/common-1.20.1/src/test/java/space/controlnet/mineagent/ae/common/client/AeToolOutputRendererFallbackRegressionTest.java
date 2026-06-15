package space.controlnet.mineagent.ae.common.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import space.controlnet.mineagent.common.client.render.ToolOutputRenderer;
import space.controlnet.mineagent.common.client.render.ToolOutputRendererRegistry;

import java.util.List;

public final class AeToolOutputRendererFallbackRegressionTest {
    @Test
    void task9_registryFallback_explicitEmptyAeResults_stillUseAeRendererOutput() {
        ToolOutputRendererRegistry.clear();
        ToolOutputRendererRegistry.register(new AeToolOutputRenderer());
        ToolOutputRendererRegistry.register(new SentinelFallbackRenderer());

        List<String> lines = ToolOutputRendererRegistry.tryRender(json("{\"results\":[],\"nextPageToken\":null,\"error\":null}"));

        assertEquals("task9/fallback/explicit-empty-results", List.of("No items found."), lines);
        ToolOutputRendererRegistry.clear();
    }

    @Test
    void task9_registryFallback_malformedAePayloads_fallThroughToLaterRenderer() {
        ToolOutputRendererRegistry.clear();
        ToolOutputRendererRegistry.register(new AeToolOutputRenderer());
        ToolOutputRendererRegistry.register(new StatusOrRawFallbackRenderer());

        JsonObject malformedWithStatus = json("{\"results\":[{\"itemId\":\"item-1\"}],\"status\":\"queued\",\"error\":\"stale-cache\"}");
        JsonObject malformedWithoutFallback = json("{\"results\":[1,2,3],\"nextPageToken\":\"n\"}");

        assertEquals(
                "task9/fallback/malformed-status-lines",
                List.of("Status: queued", "Error: stale-cache"),
                ToolOutputRendererRegistry.tryRender(malformedWithStatus)
        );
        assertEquals(
                "task9/fallback/malformed-raw-json",
                List.of(malformedWithoutFallback.toString()),
                ToolOutputRendererRegistry.tryRender(malformedWithoutFallback)
        );
        ToolOutputRendererRegistry.clear();
    }

    private static JsonObject json(String value) {
        return JsonParser.parseString(value).getAsJsonObject();
    }

    private static void assertEquals(String assertionName, Object expected, Object actual) {
        if ((expected == null && actual == null) || (expected != null && expected.equals(actual))) {
            return;
        }
        throw new AssertionError(assertionName + " -> expected: " + expected + ", actual: " + actual);
    }

    private static final class SentinelFallbackRenderer implements ToolOutputRenderer {
        @Override
        public boolean canRender(JsonObject output) {
            return true;
        }

        @Override
        public List<String> render(JsonObject output) {
            return List.of("sentinel-fallback");
        }
    }

    private static final class StatusOrRawFallbackRenderer implements ToolOutputRenderer {
        @Override
        public boolean canRender(JsonObject output) {
            return output != null;
        }

        @Override
        public List<String> render(JsonObject output) {
            String status = getString(output, "status");
            String error = getString(output, "error");
            if (status != null || error != null) {
                return List.of(
                        status == null ? "Status: " : "Status: " + status,
                        error == null ? "Error: " : "Error: " + error
                );
            }
            return List.of(output == null ? "null" : output.toString());
        }

        private static String getString(JsonObject output, String key) {
            if (output == null || key == null || !output.has(key) || output.get(key).isJsonNull()) {
                return null;
            }
            try {
                return output.get(key).getAsString();
            } catch (Exception ignored) {
                return null;
            }
        }
    }
}
