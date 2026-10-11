package com.cth.service;

import com.cth.model.AppConfig;
import com.cth.repository.AppConfigRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class ConfigService {

    private final AppConfigRepository configRepository;

    @Value("${app.name:CTH Data Reconciliation Platform}")
    private String defaultAppName;

    @Value("${app.auth-mode:DB}")
    private String defaultAuthMode;

    @Value("${app.2fa-enabled:false}")
    private boolean default2faEnabled;

    public ConfigService(AppConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    @PostConstruct
    public void initDefaults() {
        if (!configRepository.existsById("APP_NAME")) {
            configRepository.save(new AppConfig("APP_NAME", defaultAppName));
        }
        if (!configRepository.existsById("AUTH_MODE")) {
            configRepository.save(new AppConfig("AUTH_MODE", defaultAuthMode));
        }
        if (!configRepository.existsById("2FA_ENABLED")) {
            configRepository.save(new AppConfig("2FA_ENABLED", String.valueOf(default2faEnabled)));
        }
        if (!configRepository.existsById("PASSWORD_EXPIRY_DAYS")) {
            configRepository.save(new AppConfig("PASSWORD_EXPIRY_DAYS", "90"));
        }
    }

    public String getAppName() {
        return configRepository.findById("APP_NAME").map(AppConfig::getValue).orElse(defaultAppName);
    }

    public void setAppName(String appName) {
        configRepository.save(new AppConfig("APP_NAME", appName));
    }

    public String getAuthMode() {
        return configRepository.findById("AUTH_MODE").map(AppConfig::getValue).orElse(defaultAuthMode);
    }

    public void setAuthMode(String authMode) {
        configRepository.save(new AppConfig("AUTH_MODE", authMode.toUpperCase()));
    }

    public boolean is2FaEnabled() {
        return configRepository.findById("2FA_ENABLED")
                .map(c -> Boolean.parseBoolean(c.getValue()))
                .orElse(default2faEnabled);
    }

    public void set2FaEnabled(boolean enabled) {
        configRepository.save(new AppConfig("2FA_ENABLED", String.valueOf(enabled)));
    }

    public int getPasswordExpiryDays() {
        return configRepository.findById("PASSWORD_EXPIRY_DAYS")
                .map(c -> Integer.parseInt(c.getValue()))
                .orElse(90);
    }

    public void setPasswordExpiryDays(int days) {
        configRepository.save(new AppConfig("PASSWORD_EXPIRY_DAYS", String.valueOf(days)));
    }
}
