package space.controlnet.mineagent.ae.common.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.util.List;

public final class AeToolOutputRendererRegressionTest {
    @Test
    void task9_canRender_acceptsConcreteAeListsExplicitEmptyAeListsAndJobs_only() {
        AeToolOutputRenderer renderer = new AeToolOutputRenderer();

        assertFalse("task9/can-render/null", renderer.canRender(null));
        assertFalse("task9/can-render/unknown-shape", renderer.canRender(json("{\"foo\":1}")));
        assertFalse("task9/can-render/non-ae-results", renderer.canRender(json("{\"results\":[{\"itemId\":\"ae2:fluix\"}]}")));
        assertFalse("task9/can-render/bare-empty-results", renderer.canRender(json("{\"results\":[]}")));

        assertTrue("task9/can-render/ae-results",
                renderer.canRender(json("{\"results\":[{\"itemId\":\"item-1\",\"amount\":3}]}")));
        assertTrue("task9/can-render/explicit-empty-ae-results",
                renderer.canRender(json("{\"results\":[],\"nextPageToken\":null,\"error\":null}")));
        assertTrue("task9/can-render/job-status", renderer.canRender(json("{\"jobId\":\"job-7\"}")));
    }

    @Test
    void task9_renderAeList_formatsItemsPaginationErrorsAndExplicitEmptyResults() {
        AeToolOutputRenderer renderer = new AeToolOutputRenderer();
        JsonObject listPayload = json("""
                {
                  "results":[
                    {"itemId":"item-1","amount":64,"craftable":true},
                    {"itemId":"item-2","amount":2,"craftable":false}
                  ],
                  "nextPageToken":"page-2",
                  "error":"stale snapshot"
                }
                """);
        JsonObject emptyListPayload = json("{\"results\":[],\"nextPageToken\":null,\"error\":null}");

        assertEquals(
                "task9/ae-list/formatted-lines",
                List.of(
                        "Items (2):",
                        "• item-1 — 64 (craftable)",
                        "• item-2 — 2",
                        "Next page: page-2",
                        "Error: stale snapshot"
                ),
                renderer.render(listPayload)
        );
        assertEquals("task9/ae-list/explicit-empty-results", List.of("No items found."), renderer.render(emptyListPayload));
    }

    @Test
    void task9_renderAeList_truncatesAfterEightVisibleEntries() {
        AeToolOutputRenderer renderer = new AeToolOutputRenderer();
        JsonObject payload = json("""
                {
                  "results":[
                    {"itemId":"item-01","amount":1,"craftable":false},
                    {"itemId":"item-02","amount":2,"craftable":true},
                    {"itemId":"item-03","amount":3,"craftable":false},
                    {"itemId":"item-04","amount":4,"craftable":true},
                    {"itemId":"item-05","amount":5,"craftable":false},
                    {"itemId":"item-06","amount":6,"craftable":true},
                    {"itemId":"item-07","amount":7,"craftable":false},
                    {"itemId":"item-08","amount":8,"craftable":true},
                    {"itemId":"item-09","amount":9,"craftable":false},
                    {"itemId":"item-10","amount":10,"craftable":true}
                  ],
                  "nextPageToken":"8"
                }
                """);

        assertEquals(
                "task9/ae-list/truncation-lines",
                List.of(
                        "Items (10):",
                        "• item-01 — 1",
                        "• item-02 — 2 (craftable)",
                        "• item-03 — 3",
                        "• item-04 — 4 (craftable)",
                        "• item-05 — 5",
                        "• item-06 — 6 (craftable)",
                        "• item-07 — 7",
                        "• item-08 — 8 (craftable)",
                        "• +2 more",
                        "Next page: 8"
                ),
                renderer.render(payload)
        );
    }

    @Test
    void task9_renderJobStatus_formatsMissingItemsTruncatesAndHandlesMalformedFields() {
        AeToolOutputRenderer renderer = new AeToolOutputRenderer();
        JsonObject payload = json("""
                {
                  "jobId": "job-42",
                  "status": "queued",
                  "missingItems": [
                    {"itemId":"missing-01","amount":1},
                    {"itemId":"missing-02","amount":2},
                    {"itemId":"missing-03","amount":3},
                    {"itemId":"missing-04","amount":4},
                    {"itemId":"missing-05","amount":5},
                    {"itemId":"missing-06","amount":6},
                    {"itemId":"missing-07","amount":7},
                    {"itemId":"missing-08","amount":8},
                    {"itemId":"missing-09","amount":9}
                  ],
                  "error": "network"
                }
                """);

        assertEquals(
                "task9/job/missing-items-lines",
                List.of(
                        "Job job-42 — queued",
                        "Missing:",
                        "• 1x missing-01",
                        "• 2x missing-02",
                        "• 3x missing-03",
                        "• 4x missing-04",
                        "• 5x missing-05",
                        "• 6x missing-06",
                        "• 7x missing-07",
                        "• 8x missing-08",
                        "• +1 more",
                        "Error: network"
                ),
                renderer.render(payload)
        );
    }

    @Test
    void task9_render_invalidOrUnsupportedPayloads_areStable() {
        AeToolOutputRenderer renderer = new AeToolOutputRenderer();

        assertNull("task9/render/null", renderer.render(null));
        assertNull("task9/render/unsupported", renderer.render(json("{\"foo\":\"bar\"}")));
        assertNull("task9/render/bare-empty-results", renderer.render(json("{\"results\":[]}")));
        assertNull("task9/render/non-ae-results-not-rendered",
                renderer.render(json("{\"results\":[{\"itemId\":\"ae2:fluix\"}],\"nextPageToken\":\"x\",\"error\":\"y\"}")));

        JsonObject malformedList = json("{\"results\":[1,2,3],\"nextPageToken\":\"n\"}");
        assertNull("task9/render/non-object-results-not-ae-list", renderer.render(malformedList));

        JsonObject malformedJob = json("{\"jobId\":\"job-1\",\"status\":\"\",\"error\":\"\"}");
        assertEquals("task9/render/malformed-job-header", List.of("Job job-1"), renderer.render(malformedJob));
    }

    private static JsonObject json(String value) {
        return JsonParser.parseString(value).getAsJsonObject();
    }

    private static void assertTrue(String assertionName, boolean condition) {
        if (condition) {
            return;
        }
        throw new AssertionError(assertionName + " -> expected true");
    }

    private static void assertFalse(String assertionName, boolean condition) {
        if (!condition) {
            return;
        }
        throw new AssertionError(assertionName + " -> expected false");
    }

    private static void assertNull(String assertionName, Object value) {
        if (value == null) {
            return;
        }
        throw new AssertionError(assertionName + " -> expected null, actual: " + value);
    }

    private static void assertEquals(String assertionName, Object expected, Object actual) {
        if ((expected == null && actual == null) || (expected != null && expected.equals(actual))) {
            return;
        }
        throw new AssertionError(assertionName + " -> expected: " + expected + ", actual: " + actual);
    }
}
