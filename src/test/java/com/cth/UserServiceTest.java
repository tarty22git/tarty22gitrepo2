package com.cth;

import com.cth.model.User;
import com.cth.repository.UserRepository;
import com.cth.service.ConfigService;
import com.cth.service.SmsEmailService;
import com.cth.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ConfigService configService;

    @Mock
    private SmsEmailService smsEmailService;

    @InjectMocks
    private UserService userService;

    @Test
    void testPasswordComplexityEnforcement() {
        assertThrows(IllegalArgumentException.class, () -> userService.validatePasswordComplexity("short"));
        assertThrows(IllegalArgumentException.class, () -> userService.validatePasswordComplexity("no_special_char123"));
        assertDoesNotThrow(() -> userService.validatePasswordComplexity("Qwerty123$"));
    }

    @Test
    void testCreateUserWithDefaultPassword() {
        User user = User.builder()
                .username("john")
                .email("john@cth.com")
                .build();

        when(passwordEncoder.encode("Qwerty123$")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = userService.createUser(user);

        assertNotNull(created);
        assertEquals("encodedPassword", created.getPassword());
        assertEquals("ROLE_USER", created.getRole());
    }

    @Test
    void testRecordFailedAttemptAndLockout() {
        User user = User.builder()
                .username("testuser")
                .failedAttempts(4)
                .locked(false)
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        userService.recordFailedAttempt("testuser");

        assertEquals(5, user.getFailedAttempts());
        assertTrue(user.isLocked());
        verify(userRepository, times(1)).save(user);
    }
}
