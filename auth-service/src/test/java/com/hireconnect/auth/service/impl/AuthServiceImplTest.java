package com.hireconnect.auth.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hireconnect.auth.dto.request.LoginRequest;
import com.hireconnect.auth.dto.request.RefreshTokenRequest;
import com.hireconnect.auth.dto.request.RegisterRequest;
import com.hireconnect.auth.dto.response.AuthResponse;
import com.hireconnect.auth.entity.RefreshToken;
import com.hireconnect.auth.entity.Role;
import com.hireconnect.auth.entity.UserCredential;
import com.hireconnect.auth.producer.NotificationEventProducer;
import com.hireconnect.auth.repository.AuthRepository;
import com.hireconnect.auth.repository.PasswordResetOtpRepository;
import com.hireconnect.auth.security.JwtService;
import com.hireconnect.auth.service.RefreshTokenService;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private AuthRepository authRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private PasswordResetOtpRepository passwordResetOtpRepository;

    @Mock
    private NotificationEventProducer notificationEventProducer;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserCredential user;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        user = new UserCredential();
        user.setUserId(1L);
        user.setEmail("test@example.com");
        user.setPasswordHash("hashedPassword");
        user.setRole(Role.CANDIDATE);
        user.setIsActive(true);

        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password");
        registerRequest.setRole(Role.CANDIDATE);

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password");

        refreshToken = new RefreshToken();
        refreshToken.setToken("refresh_token_string");
        refreshToken.setUser(user);
    }

    @Test
    void register_Success() {
        when(authRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("hashedPassword");
        when(authRepository.save(any(UserCredential.class))).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("access_token_string");
        when(refreshTokenService.createOrUpdateRefreshToken(user)).thenReturn(refreshToken);

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("test@example.com", response.getEmail());
        assertEquals("access_token_string", response.getAccessToken());
        assertEquals("refresh_token_string", response.getRefreshToken());
    }

    @Test
    void register_EmailAlreadyExists_ThrowsException() {
        when(authRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> authService.register(registerRequest));
    }

    @Test
    void login_Success() {
        when(authRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "hashedPassword")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("access_token_string");
        when(refreshTokenService.createOrUpdateRefreshToken(user)).thenReturn(refreshToken);

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("test@example.com", response.getEmail());
        assertEquals("access_token_string", response.getAccessToken());
    }

    @Test
    void login_InvalidPassword_ThrowsException() {
        when(authRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "hashedPassword")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
    }

    @Test
    void login_UserNotFound_ThrowsException() {
        when(authRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
    }

    @Test
    void refreshToken_Success() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh_token_string");
        when(refreshTokenService.verifyRefreshToken("refresh_token_string")).thenReturn(refreshToken);
        when(jwtService.generateToken(user)).thenReturn("new_access_token");

        AuthResponse response = authService.refreshToken(request);

        assertNotNull(response);
        assertEquals("new_access_token", response.getAccessToken());
        assertEquals("refresh_token_string", response.getRefreshToken());
    }

    @Test
    void refreshToken_UserDeactivated_ThrowsException() {
        user.setIsActive(false);
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh_token_string");
        when(refreshTokenService.verifyRefreshToken("refresh_token_string")).thenReturn(refreshToken);

        assertThrows(RuntimeException.class, () -> authService.refreshToken(request));
    }

    @Test
    void forgotPassword_Success() {
        com.hireconnect.auth.dto.request.ForgotPasswordRequest request = new com.hireconnect.auth.dto.request.ForgotPasswordRequest();
        request.setEmail("test@example.com");
        when(authRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        authService.forgotPassword(request);

        verify(passwordResetOtpRepository).deleteByEmail("test@example.com");
        verify(passwordResetOtpRepository).save(any());
        verify(notificationEventProducer).sendNotification(any());
    }

    @Test
    void forgotPassword_UserNotFound_ThrowsException() {
        com.hireconnect.auth.dto.request.ForgotPasswordRequest request = new com.hireconnect.auth.dto.request.ForgotPasswordRequest();
        request.setEmail("test@example.com");
        when(authRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.forgotPassword(request));
    }

    @Test
    void resetPassword_Success() {
        com.hireconnect.auth.dto.request.ResetPasswordRequest request = new com.hireconnect.auth.dto.request.ResetPasswordRequest();
        request.setEmail("test@example.com");
        request.setOtp("123456");
        request.setNewPassword("newPass");

        com.hireconnect.auth.entity.PasswordResetOtp otp = new com.hireconnect.auth.entity.PasswordResetOtp();
        otp.setExpiryTime(java.time.LocalDateTime.now().plusMinutes(5));
        otp.setUsed(false);

        when(authRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordResetOtpRepository.findTopByEmailAndOtpAndUsedFalseOrderByIdDesc("test@example.com", "123456")).thenReturn(Optional.of(otp));
        when(passwordEncoder.encode("newPass")).thenReturn("hashedNewPass");

        authService.resetPassword(request);

        verify(authRepository).save(user);
        verify(passwordResetOtpRepository).save(otp);
        assertTrue(otp.getUsed());
    }

    @Test
    void validateToken_Success() {
        when(jwtService.isTokenValid("valid_token")).thenReturn(true);
        when(jwtService.extractUserId("valid_token")).thenReturn(1L);
        when(jwtService.extractEmail("valid_token")).thenReturn("test@example.com");
        when(jwtService.extractRole("valid_token")).thenReturn("CANDIDATE");

        com.hireconnect.auth.dto.response.TokenValidationResponse response = authService.validateToken("Bearer valid_token");

        assertTrue(response.isValid());
        assertEquals(1L, response.getUserId());
        assertEquals(Role.CANDIDATE, response.getRole());
    }

    @Test
    void validateToken_InvalidToken_ReturnsFalse() {
        when(jwtService.isTokenValid("invalid_token")).thenReturn(false);

        com.hireconnect.auth.dto.response.TokenValidationResponse response = authService.validateToken("invalid_token");

        assertFalse(response.isValid());
    }
}
