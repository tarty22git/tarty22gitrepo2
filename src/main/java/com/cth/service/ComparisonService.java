package com.cth.service;

import com.cth.dto.ComparisonResult;
import com.cth.model.ReconJobStats;
import com.cth.repository.ReconJobStatsRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ComparisonService {

    private final ReconJobStatsRepository statsRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ComparisonService(ReconJobStatsRepository statsRepository,
                             SimpMessagingTemplate messagingTemplate) {
        this.statsRepository = statsRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public ComparisonResult compareData(
            String file1Name, FileParserService.ParsedData data1,
            String file2Name, FileParserService.ParsedData data2,
            List<String> keyColumns,
            List<String> compareColumns,
            String username) {

        Map<String, Map<String, String>> map1 = buildCompositeKeyMap(data1.getRows(), keyColumns);
        Map<String, Map<String, String>> map2 = buildCompositeKeyMap(data2.getRows(), keyColumns);

        List<Map<String, Object>> matched = new ArrayList<>();
        List<Map<String, Object>> unmatched1 = new ArrayList<>();
        List<Map<String, Object>> unmatched2 = new ArrayList<>();

        for (Map.Entry<String, Map<String, String>> entry1 : map1.entrySet()) {
            String key = entry1.getKey();
            Map<String, String> row1 = entry1.getValue();

            if (map2.containsKey(key)) {
                Map<String, String> row2 = map2.get(key);
                boolean allMatched = true;
                Map<String, String> differences = new HashMap<>();

                List<String> colsToCompare = (compareColumns != null && !compareColumns.isEmpty())
                        ? compareColumns
                        : data1.getHeaders();

                for (String col : colsToCompare) {
                    String val1 = row1.getOrDefault(col, "");
                    String val2 = row2.getOrDefault(col, "");
                    if (!val1.equalsIgnoreCase(val2)) {
                        allMatched = false;
                        differences.put(col, val1 + " vs " + val2);
                    }
                }

                Map<String, Object> combined = new LinkedHashMap<>();
                combined.put("_key", key);
                combined.put("_rowFile1", row1);
                combined.put("_rowFile2", row2);
                combined.put("_isMatch", allMatched);
                combined.put("_differences", differences);

                if (allMatched) {
                    matched.add(combined);
                } else {
                    unmatched1.add(combined);
                }
            } else {
                Map<String, Object> un1 = new LinkedHashMap<>();
                un1.put("_key", key);
                un1.put("_rowFile1", row1);
                un1.put("_reason", "Missing in File 2");
                unmatched1.add(un1);
            }
        }

        for (Map.Entry<String, Map<String, String>> entry2 : map2.entrySet()) {
            String key = entry2.getKey();
            if (!map1.containsKey(key)) {
                Map<String, Object> un2 = new LinkedHashMap<>();
                un2.put("_key", key);
                un2.put("_rowFile2", entry2.getValue());
                un2.put("_reason", "Missing in File 1");
                unmatched2.add(un2);
            }
        }

        ComparisonResult result = ComparisonResult.builder()
                .file1Name(file1Name)
                .file2Name(file2Name)
                .headers1(data1.getHeaders())
                .headers2(data2.getHeaders())
                .keyColumns(keyColumns)
                .compareColumns(compareColumns)
                .totalFile1Rows(data1.getRows().size())
                .totalFile2Rows(data2.getRows().size())
                .matchedCount(matched.size())
                .unmatchedCount(unmatched1.size() + unmatched2.size())
                .matchedRows(matched)
                .unmatchedFile1Rows(unmatched1)
                .unmatchedFile2Rows(unmatched2)
                .build();

        // Save execution stats
        ReconJobStats stats = ReconJobStats.builder()
                .jobName("Reconcile: " + file1Name + " vs " + file2Name)
                .file1Name(file1Name)
                .file2Name(file2Name)
                .matchedCount(result.getMatchedCount())
                .unmatchedCount(result.getUnmatchedCount())
                .status("SUCCESS")
                .executedBy(username)
                .build();
        statsRepository.save(stats);

        // Notify dashboard WebSocket
        try {
            messagingTemplate.convertAndSend("/topic/dashboard-updates", stats);
        } catch (Exception ignored) {}

        return result;
    }

    private Map<String, Map<String, String>> buildCompositeKeyMap(
            List<Map<String, String>> rows, List<String> keyColumns) {
        Map<String, Map<String, String>> map = new LinkedHashMap<>();
        int index = 1;
        for (Map<String, String> row : rows) {
            StringBuilder keyBuilder = new StringBuilder();
            if (keyColumns != null && !keyColumns.isEmpty()) {
                for (String k : keyColumns) {
                    keyBuilder.append(row.getOrDefault(k, "").trim().toLowerCase()).append("||");
                }
            } else {
                keyBuilder.append("ROW_").append(index);
            }
            map.put(keyBuilder.toString(), row);
            index++;
        }
        return map;
    }
}
