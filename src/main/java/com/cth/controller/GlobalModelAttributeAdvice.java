package com.cth.controller;

import com.cth.service.ConfigService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributeAdvice {

    private final ConfigService configService;

    public GlobalModelAttributeAdvice(ConfigService configService) {
        this.configService = configService;
    }

    @ModelAttribute("appName")
    public String appName() {
        return configService.getAppName();
    }
}
