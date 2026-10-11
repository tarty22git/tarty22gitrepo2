package com.cth.controller;

import com.cth.service.DynamicReportService;
import com.cth.service.SchemaIntrospectionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final SchemaIntrospectionService schemaService;
    private final DynamicReportService reportService;

    public ReportController(SchemaIntrospectionService schemaService, DynamicReportService reportService) {
        this.schemaService = schemaService;
        this.reportService = reportService;
    }

    @GetMapping
    public String index(Model model) {
        List<String> tables = schemaService.getAvailableTables();
        model.addAttribute("tables", tables);
        return "reports";
    }

    @GetMapping("/api/columns")
    @ResponseBody
    public List<String> getColumns(@RequestParam("table") String tableName) {
        return schemaService.getTableColumns(tableName);
    }

    @PostMapping("/generate")
    public String generateReport(
            @RequestParam("tableName") String tableName,
            @RequestParam(value = "fields", required = false) String[] fields,
            @RequestParam(value = "filterCondition", required = false) String filterCondition,
            @RequestParam(value = "limit", defaultValue = "100") int limit,
            Model model) {

        List<String> tables = schemaService.getAvailableTables();
        model.addAttribute("tables", tables);
        model.addAttribute("selectedTable", tableName);

        try {
            List<String> fieldList = fields != null ? Arrays.asList(fields) : List.of();
            List<Map<String, Object>> rows = reportService.executeQuery(tableName, fieldList, filterCondition, limit);

            model.addAttribute("rows", rows);
            if (!rows.isEmpty()) {
                model.addAttribute("headers", rows.get(0).keySet());
            }
            model.addAttribute("totalCount", rows.size());
        } catch (Exception e) {
            model.addAttribute("error", "Failed to generate report: " + e.getMessage());
        }

        return "reports";
    }
}
