package com.cth.controller;

import com.cth.service.ConfigService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/settings")
public class SettingsController {

    private final ConfigService configService;

    public SettingsController(ConfigService configService) {
        this.configService = configService;
    }

    @GetMapping
    public String showSettings(Model model) {
        model.addAttribute("appNameVal", configService.getAppName());
        model.addAttribute("authMode", configService.getAuthMode());
        model.addAttribute("twoFaEnabled", configService.is2FaEnabled());
        model.addAttribute("passwordExpiryDays", configService.getPasswordExpiryDays());
        return "settings";
    }

    @PostMapping("/update")
    public String updateSettings(
            @RequestParam("appName") String appName,
            @RequestParam("authMode") String authMode,
            @RequestParam(value = "twoFaEnabled", defaultValue = "false") boolean twoFaEnabled,
            @RequestParam("passwordExpiryDays") int passwordExpiryDays,
            RedirectAttributes redirectAttributes) {
        try {
            configService.setAppName(appName);
            configService.setAuthMode(authMode);
            configService.set2FaEnabled(twoFaEnabled);
            configService.setPasswordExpiryDays(passwordExpiryDays);
            redirectAttributes.addFlashAttribute("success", "Settings updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update settings: " + e.getMessage());
        }
        return "redirect:/settings";
    }
}
