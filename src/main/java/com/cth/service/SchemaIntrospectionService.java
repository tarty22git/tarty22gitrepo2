package com.cth.service;

import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.*;

@Service
public class SchemaIntrospectionService {

    private final DataSource dataSource;

    public SchemaIntrospectionService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<String> getAvailableTables() {
        List<String> tables = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getTables(null, null, "%", new String[]{"TABLE", "VIEW"})) {
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    if (!tableName.startsWith("SYSTEM_") && !tableName.startsWith("SYS")) {
                        tables.add(tableName);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tables;
    }

    public List<String> getTableColumns(String tableName) {
        List<String> columns = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getColumns(null, null, tableName, "%")) {
                while (rs.next()) {
                    columns.add(rs.getString("COLUMN_NAME"));
                }
            }
            if (columns.isEmpty()) {
                // Try uppercase
                try (ResultSet rs = metaData.getColumns(null, null, tableName.toUpperCase(), "%")) {
                    while (rs.next()) {
                        columns.add(rs.getString("COLUMN_NAME"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return columns;
    }
}
