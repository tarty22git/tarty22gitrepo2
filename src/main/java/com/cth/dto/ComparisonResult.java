package com.cth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
public class ComparisonResult {
    private String file1Name;
    private String file2Name;
    private List<String> headers1;
    private List<String> headers2;
    private List<String> keyColumns;
    private List<String> compareColumns;

    private int totalFile1Rows;
    private int totalFile2Rows;
    private int matchedCount;
    private int unmatchedCount;

    private List<Map<String, Object>> matchedRows;
    private List<Map<String, Object>> unmatchedFile1Rows;
    private List<Map<String, Object>> unmatchedFile2Rows;
}
