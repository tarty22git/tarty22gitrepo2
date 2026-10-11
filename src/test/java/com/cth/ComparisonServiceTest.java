package com.cth;

import com.cth.dto.ComparisonResult;
import com.cth.repository.ReconJobStatsRepository;
import com.cth.service.ComparisonService;
import com.cth.service.FileParserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ComparisonServiceTest {

    @Mock
    private ReconJobStatsRepository statsRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private ComparisonService comparisonService;

    @Test
    void testCompareDataMatchedAndUnmatched() {
        List<String> headers = List.of("ID", "Name", "Amount");

        Map<String, String> row1 = Map.of("ID", "101", "Name", "Alice", "Amount", "100.00");
        Map<String, String> row2 = Map.of("ID", "102", "Name", "Bob", "Amount", "200.00");

        Map<String, String> row1File2 = Map.of("ID", "101", "Name", "Alice", "Amount", "100.00");
        Map<String, String> row2File2 = Map.of("ID", "102", "Name", "Bob", "Amount", "250.00"); // Different amount

        FileParserService.ParsedData data1 = new FileParserService.ParsedData(headers, List.of(row1, row2));
        FileParserService.ParsedData data2 = new FileParserService.ParsedData(headers, List.of(row1File2, row2File2));

        ComparisonResult result = comparisonService.compareData(
                "file1.csv", data1,
                "file2.csv", data2,
                List.of("ID"),
                List.of("Name", "Amount"),
                "admin"
        );

        assertNotNull(result);
        assertEquals(1, result.getMatchedCount());
        assertEquals(1, result.getUnmatchedCount());
    }
}
