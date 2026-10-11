package com.cth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class SmsEmailService {

    private static final Logger log = LoggerFactory.getLogger(SmsEmailService.class);
    private final ConfigService configService;
    private final SecureRandom random = new SecureRandom();

    public SmsEmailService(ConfigService configService) {
        this.configService = configService;
    }

    public String generateOtp() {
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    public void sendOtpSmsOrEmail(String destination, String otp) {
        if (!configService.is2FaEnabled()) {
            log.info("2FA/SMS/Email verification is currently DISABLED in application configuration.");
            return;
        }
        log.info("==================================================================");
        log.info(" [SIMULATED 2FA OTP AUTHENTICATION] ");
        log.info(" Target Destination: {}", destination);
        log.info(" Security OTP Code : {}", otp);
        log.info(" Expiry Time       : {}", LocalDateTime.now().plusMinutes(5));
        log.info("==================================================================");
    }
}
