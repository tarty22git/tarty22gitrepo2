package com.cth.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

@Service
public class FileParserService {

    public static class ParsedData {
        private List<String> headers;
        private List<Map<String, String>> rows;

        public ParsedData(List<String> headers, List<Map<String, String>> rows) {
            this.headers = headers;
            this.rows = rows;
        }

        public List<String> getHeaders() {
            return headers;
        }

        public List<Map<String, String>> getRows() {
            return rows;
        }
    }

    public ParsedData parseFile(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("Invalid file name.");
        }
        String lower = filename.toLowerCase();
        if (lower.endsWith(".csv")) {
            return parseCsv(file.getInputStream());
        } else if (lower.endsWith(".xlsx") || lower.endsWith(".xls")) {
            return parseExcel(file.getInputStream());
        } else {
            throw new IllegalArgumentException("Unsupported file type. Please upload a CSV or Excel file.");
        }
    }

    public ParsedData parseCsv(InputStream inputStream) throws Exception {
        try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream))) {
            List<String[]> allData = reader.readAll();
            if (allData.isEmpty()) {
                return new ParsedData(Collections.emptyList(), Collections.emptyList());
            }

            String[] headerArray = allData.get(0);
            List<String> headers = new ArrayList<>();
            for (String h : headerArray) {
                headers.add(h != null ? h.trim() : "");
            }

            List<Map<String, String>> rows = new ArrayList<>();
            for (int i = 1; i < allData.size(); i++) {
                String[] rowArray = allData.get(i);
                Map<String, String> rowMap = new LinkedHashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    String val = (j < rowArray.length) ? rowArray[j].trim() : "";
                    rowMap.put(headers.get(j), val);
                }
                rows.add(rowMap);
            }
            return new ParsedData(headers, rows);
        }
    }

    public ParsedData parseExcel(InputStream inputStream) throws Exception {
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            if (!rowIterator.hasNext()) {
                return new ParsedData(Collections.emptyList(), Collections.emptyList());
            }

            Row headerRow = rowIterator.next();
            List<String> headers = new ArrayList<>();
            DataFormatter formatter = new DataFormatter();

            for (Cell cell : headerRow) {
                headers.add(formatter.formatCellValue(cell).trim());
            }

            List<Map<String, String>> rows = new ArrayList<>();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Map<String, String> rowMap = new LinkedHashMap<>();
                boolean emptyRow = true;
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j);
                    String val = formatter.formatCellValue(cell).trim();
                    if (!val.isEmpty()) {
                        emptyRow = false;
                    }
                    rowMap.put(headers.get(j), val);
                }
                if (!emptyRow) {
                    rows.add(rowMap);
                }
            }
            return new ParsedData(headers, rows);
        }
    }
}
