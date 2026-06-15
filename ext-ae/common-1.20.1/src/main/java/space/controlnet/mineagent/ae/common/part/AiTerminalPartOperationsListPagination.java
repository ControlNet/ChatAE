package space.controlnet.mineagent.ae.common.part;

import org.jetbrains.annotations.Nullable;
import space.controlnet.mineagent.ae.core.terminal.AiTerminalData;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

final class AiTerminalPartOperationsListPagination {
    private AiTerminalPartOperationsListPagination() {
    }

    static AiTerminalData.AeListResult paginate(
            List<AiTerminalData.AeEntry> entries,
            @Nullable String query,
            boolean craftableOnly,
            int limit,
            @Nullable String pageToken
    ) {
        String normalizedQuery = query == null ? "" : query.toLowerCase(Locale.ROOT).trim();
        int safeLimit = Math.max(1, Math.min(limit, 200));
        int offset = parseOffsetNullable(pageToken).orElse(0);

        List<AiTerminalData.AeEntry> filtered = entries.stream()
                .filter(entry -> normalizedQuery.isEmpty() || entry.itemId().toLowerCase(Locale.ROOT).contains(normalizedQuery))
                .filter(entry -> !craftableOnly || entry.craftable())
                .sorted(Comparator.comparing(AiTerminalData.AeEntry::itemId))
                .toList();

        if (offset >= filtered.size()) {
            return new AiTerminalData.AeListResult(List.of(), Optional.empty(), Optional.empty());
        }

        int endExclusive = Math.min(offset + safeLimit, filtered.size());
        Optional<String> nextPageToken = endExclusive < filtered.size()
                ? Optional.of(Integer.toString(endExclusive))
                : Optional.empty();
        return new AiTerminalData.AeListResult(filtered.subList(offset, endExclusive), nextPageToken, Optional.empty());
    }

    private static Optional<Integer> parseOffset(String token) {
        try {
            int offset = Integer.parseInt(token);
            return offset < 0 ? Optional.empty() : Optional.of(offset);
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    private static Optional<Integer> parseOffsetNullable(@Nullable String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return parseOffset(token);
    }
}
