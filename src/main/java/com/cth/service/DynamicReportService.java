package com.cth.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class DynamicReportService {

    private final JdbcTemplate jdbcTemplate;
    private final SchemaIntrospectionService schemaService;
    private static final Pattern SAFE_IDENTIFIER_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");

    public DynamicReportService(JdbcTemplate jdbcTemplate, SchemaIntrospectionService schemaService) {
        this.jdbcTemplate = jdbcTemplate;
        this.schemaService = schemaService;
    }

    public List<Map<String, Object>> executeQuery(String tableName, List<String> fields, String filterCondition, int limit) {
        if (tableName == null || tableName.isBlank() || !SAFE_IDENTIFIER_PATTERN.matcher(tableName).matches()) {
            throw new IllegalArgumentException("Invalid or unsafe table name.");
        }

        List<String> validTables = schemaService.getAvailableTables();
        boolean exists = validTables.stream().anyMatch(t -> t.equalsIgnoreCase(tableName));
        if (!exists) {
            throw new IllegalArgumentException("Table does not exist in schema: " + tableName);
        }

        List<String> validColumns = schemaService.getTableColumns(tableName);
        List<String> safeFields = new ArrayList<>();

        if (fields != null && !fields.isEmpty()) {
            for (String f : fields) {
                if (SAFE_IDENTIFIER_PATTERN.matcher(f).matches() &&
                        validColumns.stream().anyMatch(c -> c.equalsIgnoreCase(f))) {
                    safeFields.add(f);
                }
            }
        }

        String selectedFieldsStr = safeFields.isEmpty() ? "*" : String.join(", ", safeFields);

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ").append(selectedFieldsStr).append(" FROM ").append(tableName);

        // Sanitize filter condition to avoid SQL injection
        if (filterCondition != null && !filterCondition.isBlank()) {
            String sanitizedFilter = sanitizeFilter(filterCondition, validColumns);
            if (!sanitizedFilter.isBlank()) {
                sql.append(" WHERE ").append(sanitizedFilter);
            }
        }

        if (limit > 0) {
            sql.append(" FETCH FIRST ").append(Math.min(limit, 1000)).append(" ROWS ONLY");
        }

        try {
            return jdbcTemplate.queryForList(sql.toString());
        } catch (Exception e) {
            String fallbackSql = "SELECT " + selectedFieldsStr + " FROM " + tableName +
                    (filterCondition != null && !filterCondition.isBlank() ? " WHERE " + sanitizeFilter(filterCondition, validColumns) : "") +
                    (limit > 0 ? " LIMIT " + Math.min(limit, 1000) : "");
            return jdbcTemplate.queryForList(fallbackSql);
        }
    }

    private String sanitizeFilter(String filter, List<String> validColumns) {
        // Strip dangerous SQL keywords that modify schema or data
        String upper = filter.toUpperCase();
        if (upper.contains("DROP") || upper.contains("DELETE") || upper.contains("UPDATE") ||
                upper.contains("INSERT") || upper.contains("ALTER") || upper.contains("TRUNCATE") ||
                upper.contains("EXEC") || upper.contains(";")) {
            throw new IllegalArgumentException("Illegal characters or keywords detected in filter condition.");
        }
        return filter.replaceAll("['\"]--", "").trim();
    }
}
