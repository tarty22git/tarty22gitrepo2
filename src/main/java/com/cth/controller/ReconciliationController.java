package com.cth.controller;

import com.cth.dto.ComparisonResult;
import com.cth.service.ComparisonService;
import com.cth.service.FileParserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/reconcile")
public class ReconciliationController {

    private final FileParserService fileParserService;
    private final ComparisonService comparisonService;

    public ReconciliationController(FileParserService fileParserService, ComparisonService comparisonService) {
        this.fileParserService = fileParserService;
        this.comparisonService = comparisonService;
    }

    @GetMapping
    public String showReconcileForm() {
        return "reconcile";
    }

    @PostMapping("/analyze")
    public String analyzeFiles(
            @RequestParam("file1") MultipartFile file1,
            @RequestParam("file2") MultipartFile file2,
            Model model) {
        try {
            FileParserService.ParsedData data1 = fileParserService.parseFile(file1);
            FileParserService.ParsedData data2 = fileParserService.parseFile(file2);

            model.addAttribute("file1Name", file1.getOriginalFilename());
            model.addAttribute("file2Name", file2.getOriginalFilename());
            model.addAttribute("headers1", data1.getHeaders());
            model.addAttribute("headers2", data2.getHeaders());
            model.addAttribute("analyzed", true);

            // Store files temporarily in session if needed, or pass file parsed stats
            return "reconcile";
        } catch (Exception e) {
            model.addAttribute("error", "Error parsing files: " + e.getMessage());
            return "reconcile";
        }
    }

    @PostMapping("/compare")
    public String compareFiles(
            @RequestParam("file1") MultipartFile file1,
            @RequestParam("file2") MultipartFile file2,
            @RequestParam(value = "keyColumns", required = false) String[] keyColumns,
            @RequestParam(value = "compareColumns", required = false) String[] compareColumns,
            Model model,
            Authentication authentication) {
        try {
            FileParserService.ParsedData data1 = fileParserService.parseFile(file1);
            FileParserService.ParsedData data2 = fileParserService.parseFile(file2);

            List<String> keys = keyColumns != null ? Arrays.asList(keyColumns) : List.of();
            List<String> compareCols = compareColumns != null ? Arrays.asList(compareColumns) : List.of();

            String username = authentication != null ? authentication.getName() : "System";

            ComparisonResult result = comparisonService.compareData(
                    file1.getOriginalFilename(), data1,
                    file2.getOriginalFilename(), data2,
                    keys, compareCols, username
            );

            model.addAttribute("result", result);
            return "reconcile-result";
        } catch (Exception e) {
            model.addAttribute("error", "Comparison failed: " + e.getMessage());
            return "reconcile";
        }
    }
}
