package com.cth.controller;

import com.cth.model.ReconJobStats;
import com.cth.repository.ReconJobStatsRepository;
import com.cth.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardController {

    private final ReconJobStatsRepository statsRepository;
    private final UserService userService;

    public DashboardController(ReconJobStatsRepository statsRepository, UserService userService) {
        this.statsRepository = statsRepository;
        this.userService = userService;
    }

    @GetMapping("/")
    public String dashboard(Model model, Authentication authentication) {
        List<ReconJobStats> recentJobs = statsRepository.findTop10ByOrderByCreatedAtDesc();
        long totalJobs = statsRepository.count();
        long totalUsers = userService.getAllUsers().size();

        int totalMatched = recentJobs.stream().mapToInt(ReconJobStats::getMatchedCount).sum();
        int totalUnmatched = recentJobs.stream().mapToInt(ReconJobStats::getUnmatchedCount).sum();

        model.addAttribute("recentJobs", recentJobs);
        model.addAttribute("totalJobs", totalJobs);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalMatched", totalMatched);
        model.addAttribute("totalUnmatched", totalUnmatched);
        model.addAttribute("currentUser", authentication != null ? authentication.getName() : "User");

        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
