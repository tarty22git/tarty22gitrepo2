package com.cth;

import com.cth.service.FileParserService;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class FileParserServiceTest {

    private final FileParserService parserService = new FileParserService();

    @Test
    void testParseCsv() throws Exception {
        String csvContent = "ID,Name,Email\n1,Alice,alice@cth.com\n2,Bob,bob@cth.com\n";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));

        FileParserService.ParsedData parsedData = parserService.parseCsv(inputStream);

        assertNotNull(parsedData);
        assertEquals(3, parsedData.getHeaders().size());
        assertEquals(2, parsedData.getRows().size());
        assertEquals("Alice", parsedData.getRows().get(0).get("Name"));
    }
}
