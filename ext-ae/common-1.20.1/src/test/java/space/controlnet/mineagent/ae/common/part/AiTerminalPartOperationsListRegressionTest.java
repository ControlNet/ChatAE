package space.controlnet.mineagent.ae.common.part;

import org.junit.jupiter.api.Test;
import space.controlnet.mineagent.ae.core.terminal.AiTerminalData;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public final class AiTerminalPartOperationsListRegressionTest {
    @Test
    void task6_listItems_queryFilteringSortedOrderAndNullPageToken_areStable() {
        List<AiTerminalData.AeEntry> entries = List.of(
                new AiTerminalData.AeEntry("minecraft:copper_ingot", 7L, false),
                new AiTerminalData.AeEntry("minecraft:apple", 2L, false),
                new AiTerminalData.AeEntry("minecraft:copper_block", 5L, true)
        );

        AiTerminalData.AeListResult firstPage = AiTerminalPartOperationsListPagination.paginate(entries, "  COPPER  ", false, 1, null);
        AiTerminalData.AeListResult secondPage = AiTerminalPartOperationsListPagination.paginate(entries, "copper", false, 1, "1");

        assertEquals("task6/list-items/null-page/results", List.of(
                new AiTerminalData.AeEntry("minecraft:copper_block", 5L, true)
        ), firstPage.results());
        assertEquals("task6/list-items/null-page/next", Optional.of("1"), firstPage.nextPageToken());
        assertEquals("task6/list-items/null-page/error", Optional.empty(), firstPage.error());
        assertEquals("task6/list-items/second-page/results", List.of(
                new AiTerminalData.AeEntry("minecraft:copper_ingot", 7L, false)
        ), secondPage.results());
        assertEquals("task6/list-items/second-page/next", Optional.empty(), secondPage.nextPageToken());
    }

    @Test
    void task6_listItems_craftableOnlyBlankInvalidAndNegativeTokens_useFilteredPaginationAndClampLimit() {
        List<AiTerminalData.AeEntry> entries = List.of(
                new AiTerminalData.AeEntry("minecraft:apple", 1L, false),
                new AiTerminalData.AeEntry("minecraft:bread", 2L, true),
                new AiTerminalData.AeEntry("minecraft:carrot", 3L, true),
                new AiTerminalData.AeEntry("minecraft:diamond", 4L, false)
        );

        AiTerminalData.AeListResult blankTokenPage = AiTerminalPartOperationsListPagination.paginate(entries, "", true, 0, "   ");
        AiTerminalData.AeListResult nextPage = AiTerminalPartOperationsListPagination.paginate(entries, "", true, 1, "1");
        AiTerminalData.AeListResult invalidTokenPage = AiTerminalPartOperationsListPagination.paginate(entries, "", true, 5, "bogus");
        AiTerminalData.AeListResult negativeTokenPage = AiTerminalPartOperationsListPagination.paginate(entries, "", true, 5, "-1");

        assertEquals("task6/list-items/blank-token/results", List.of(
                new AiTerminalData.AeEntry("minecraft:bread", 2L, true)
        ), blankTokenPage.results());
        assertEquals("task6/list-items/blank-token/next", Optional.of("1"), blankTokenPage.nextPageToken());
        assertEquals("task6/list-items/blank-token/error", Optional.empty(), blankTokenPage.error());
        assertEquals("task6/list-items/craftable-next-page/results", List.of(
                new AiTerminalData.AeEntry("minecraft:carrot", 3L, true)
        ), nextPage.results());
        assertEquals("task6/list-items/craftable-next-page/next", Optional.empty(), nextPage.nextPageToken());
        assertEquals("task6/list-items/invalid-token/results", List.of(
                new AiTerminalData.AeEntry("minecraft:bread", 2L, true),
                new AiTerminalData.AeEntry("minecraft:carrot", 3L, true)
        ), invalidTokenPage.results());
        assertEquals("task6/list-items/invalid-token/next", Optional.empty(), invalidTokenPage.nextPageToken());
        assertEquals("task6/list-items/negative-token/results", List.of(
                new AiTerminalData.AeEntry("minecraft:bread", 2L, true),
                new AiTerminalData.AeEntry("minecraft:carrot", 3L, true)
        ), negativeTokenPage.results());
        assertEquals("task6/list-items/negative-token/next", Optional.empty(), negativeTokenPage.nextPageToken());
    }

    @Test
    void task6_listCraftables_queryFilteringSortedOrderAndBlankInvalidTokens_areStable() {
        List<AiTerminalData.AeEntry> entries = List.of(
                new AiTerminalData.AeEntry("minecraft:copper_ingot", 0L, true),
                new AiTerminalData.AeEntry("minecraft:apple", 0L, true),
                new AiTerminalData.AeEntry("minecraft:copper_block", 0L, true)
        );

        AiTerminalData.AeListResult blankTokenPage = AiTerminalPartOperationsListPagination.paginate(entries, "  COPPER  ", false, 10, " ");
        AiTerminalData.AeListResult invalidTokenPage = AiTerminalPartOperationsListPagination.paginate(entries, "copper", false, 10, "bad-token");

        List<AiTerminalData.AeEntry> expected = List.of(
                new AiTerminalData.AeEntry("minecraft:copper_block", 0L, true),
                new AiTerminalData.AeEntry("minecraft:copper_ingot", 0L, true)
        );

        assertEquals("task6/list-craftables/blank-token/results", expected, blankTokenPage.results());
        assertEquals("task6/list-craftables/blank-token/next", Optional.empty(), blankTokenPage.nextPageToken());
        assertEquals("task6/list-craftables/invalid-token/results", expected, invalidTokenPage.results());
        assertEquals("task6/list-craftables/invalid-token/next", Optional.empty(), invalidTokenPage.nextPageToken());
        assertEquals("task6/list-craftables/error", Optional.empty(), blankTokenPage.error());
    }

    @Test
    void task6_listCraftables_upperLimitClampAndNextPageToken_areStable() {
        List<AiTerminalData.AeEntry> craftables = IntStream.range(0, 205)
                .mapToObj(index -> new AiTerminalData.AeEntry("item:" + String.format("%03d", 204 - index), 0L, true))
                .toList();
        assertEquals("task6/list-craftables/craftables-size", 205, craftables.size());

        List<AiTerminalData.AeEntry> sortedCraftables = craftables.stream()
                .sorted(java.util.Comparator.comparing(AiTerminalData.AeEntry::itemId))
                .toList();

        AiTerminalData.AeListResult firstPage = AiTerminalPartOperationsListPagination.paginate(craftables, "", false, 500, null);
        AiTerminalData.AeListResult secondPage = AiTerminalPartOperationsListPagination.paginate(craftables, "", false, 500, "200");

        assertEquals("task6/list-craftables/upper-clamp/first-page-size", 200, firstPage.results().size());
        assertEquals("task6/list-craftables/upper-clamp/first-page-results", sortedCraftables.subList(0, 200), firstPage.results());
        assertEquals("task6/list-craftables/upper-clamp/first-page-next", Optional.of("200"), firstPage.nextPageToken());
        assertEquals("task6/list-craftables/upper-clamp/second-page-results", sortedCraftables.subList(200, 205), secondPage.results());
        assertEquals("task6/list-craftables/upper-clamp/second-page-next", Optional.empty(), secondPage.nextPageToken());
        assertEquals("task6/list-craftables/upper-clamp/error", Optional.empty(), firstPage.error());
    }

    private static void assertEquals(String assertionName, Object expected, Object actual) {
        if ((expected == null && actual == null) || (expected != null && expected.equals(actual))) {
            return;
        }
        throw new AssertionError(assertionName + " -> expected: " + expected + ", actual: " + actual);
    }
}
