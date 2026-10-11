package com.cth.service;

import com.cth.model.User;
import com.cth.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    public static final String DEFAULT_PASSWORD = "Qwerty123$";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ConfigService configService;
    private final SmsEmailService smsEmailService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       ConfigService configService,
                       SmsEmailService smsEmailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.configService = configService;
        this.smsEmailService = smsEmailService;
    }

    @PostConstruct
    public void initDefaultUsers() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                    .email("admin@cth.com")
                    .phoneNumber("+1234567890")
                    .role("ROLE_ADMIN")
                    .enabled(true)
                    .locked(false)
                    .failedAttempts(0)
                    .passwordLastUpdated(LocalDateTime.now())
                    .build();
            userRepository.save(admin);
        }
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public User createUser(User user) {
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            user.setPassword(DEFAULT_PASSWORD);
        }
        validatePasswordComplexity(user.getPassword());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setPasswordLastUpdated(LocalDateTime.now());
        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("ROLE_USER");
        }

        if (configService.is2FaEnabled()) {
            String otp = smsEmailService.generateOtp();
            user.setOtpCode(otp);
            user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
            smsEmailService.sendOtpSmsOrEmail(user.getEmail(), otp);
        }

        return userRepository.save(user);
    }

    @Transactional
    public void toggleUserLock(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setLocked(!user.isLocked());
            if (!user.isLocked()) {
                user.setFailedAttempts(0);
            }
            userRepository.save(user);
        });
    }

    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        validatePasswordComplexity(newPassword);
        userRepository.findById(userId).ifPresent(user -> {
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setPasswordLastUpdated(LocalDateTime.now());
            user.setFailedAttempts(0);
            user.setLocked(false);
            userRepository.save(user);
        });
    }

    public boolean isPasswordExpired(User user) {
        if (user.getPasswordLastUpdated() == null) return false;
        int expiryDays = configService.getPasswordExpiryDays();
        return user.getPasswordLastUpdated().plusDays(expiryDays).isBefore(LocalDateTime.now());
    }

    public void validatePasswordComplexity(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long.");
        }
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(ch -> "!@#$%^&*()_+-=[]{}|;:,.<>?".indexOf(ch) >= 0);

        if (!hasUpper || !hasLower || !hasDigit || !hasSpecial) {
            throw new IllegalArgumentException("Password must contain uppercase, lowercase, digit, and special character.");
        }
    }

    @Transactional
    public void recordFailedAttempt(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            int attempts = user.getFailedAttempts() + 1;
            user.setFailedAttempts(attempts);
            if (attempts >= 5) {
                user.setLocked(true);
            }
            userRepository.save(user);
        });
    }

    @Transactional
    public void resetFailedAttempts(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setFailedAttempts(0);
            userRepository.save(user);
        });
    }
}
