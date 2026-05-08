package com.hireconnect.auth.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hireconnect.auth.client.NotificationServiceClient;
import com.hireconnect.auth.dto.request.ForgotPasswordRequest;
import com.hireconnect.auth.dto.request.LoginRequest;
import com.hireconnect.auth.dto.request.OtpRequest;
import com.hireconnect.auth.dto.request.RefreshTokenRequest;
import com.hireconnect.auth.dto.request.RegisterRequest;
import com.hireconnect.auth.dto.request.ResetPasswordRequest;
import com.hireconnect.auth.dto.response.AdminUserResponse;
import com.hireconnect.auth.dto.response.AuthResponse;
import com.hireconnect.auth.dto.response.TokenValidationResponse;
import com.hireconnect.auth.entity.AuthProvider;
import com.hireconnect.auth.entity.NotificationType;
import com.hireconnect.auth.entity.OtpVerification;
import com.hireconnect.auth.entity.PasswordResetOtp;
import com.hireconnect.auth.entity.RefreshToken;
import com.hireconnect.auth.entity.Role;
import com.hireconnect.auth.entity.UserCredential;
import com.hireconnect.auth.event.NotificationEvent;
import com.hireconnect.auth.producer.NotificationEventProducer;
import com.hireconnect.auth.repository.AuthRepository;
import com.hireconnect.auth.repository.OtpVerificationRepository;
import com.hireconnect.auth.repository.PasswordResetOtpRepository;
import com.hireconnect.auth.repository.RefreshTokenRepository;
import com.hireconnect.auth.security.JwtService;
import com.hireconnect.auth.service.AuthService;
import com.hireconnect.auth.service.RefreshTokenService;
import com.hireconnect.auth.security.AuthenticatedUser;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordResetOtpRepository passwordResetOtpRepository;
    private final NotificationEventProducer notificationEventProducer;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OtpVerificationRepository otpVerificationRepository;
    private final NotificationServiceClient notificationServiceClient;

    @Value("${auth.reset-otp-expiration-minutes:10}")
    private long resetOtpExpirationMinutes;

    @Override
    @Transactional
    public void requestOtp(OtpRequest request) {
        log.info("Requesting OTP for email: {}", request.getEmail());

        if (authRepository.existsByEmail(request.getEmail())) {
            log.warn("OTP request failed because email is already registered: {}", request.getEmail());
            throw new RuntimeException("Email is already registered");
        }

        otpVerificationRepository.deleteByEmail(request.getEmail());

        String otp = String.valueOf((int) ((Math.random() * 900000) + 100000));
        
        OtpVerification otpVerification = OtpVerification.builder()
                .email(request.getEmail())
                .otp(otp)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();
                
        otpVerificationRepository.save(otpVerification);
        
        String message = "Your registration OTP is: " + otp + ". It is valid for 10 minutes.";
        NotificationServiceClient.SendEmailRequestDto emailRequest = new NotificationServiceClient.SendEmailRequestDto();
        emailRequest.setTo(request.getEmail());
        emailRequest.setSubject("Registration OTP");
        emailRequest.setBody(message);
        
        notificationServiceClient.sendEmail(emailRequest);
        log.info("OTP sent to email: {}", request.getEmail());
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registration started for email: {}, role: {}", request.getEmail(), request.getRole());

        if (authRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed because email is already registered: {}", request.getEmail());
            throw new RuntimeException("Email is already registered");
        }
        
        OtpVerification otpVerification = otpVerificationRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("No OTP requested for this email"));
                
        if (!otpVerification.getOtp().equals(request.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }
        
        if (otpVerification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP has expired");
        }
        
        otpVerificationRepository.deleteByEmail(request.getEmail());

        UserCredential user = UserCredential.builder()
                .email(request.getEmail())
                .fullName(resolveFullName(request.getFullName(), request.getEmail()))
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .provider(AuthProvider.LOCAL)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        UserCredential savedUser = authRepository.save(user);
        log.info("User registered successfully with userId: {}, email: {}, role: {}",
                savedUser.getUserId(), savedUser.getEmail(), savedUser.getRole());

        String accessToken = jwtService.generateToken(savedUser);
        RefreshToken refreshToken = refreshTokenService.createOrUpdateRefreshToken(savedUser);

        return AuthResponse.builder()
                .userId(savedUser.getUserId())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .role(savedUser.getRole())
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .message(savedUser.getRole() + " registered successfully")
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Login started for email: {}", request.getEmail());

        UserCredential user = authRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed. User not found for email: {}", request.getEmail());
                    return new RuntimeException("Invalid email or password");
                });

        if (!user.getIsActive()) {
            log.warn("Login failed because account is deactivated for userId: {}, email: {}",
                    user.getUserId(), user.getEmail());
            throw new RuntimeException("Account is deactivated");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Login failed due to invalid password for userId: {}, email: {}",
                    user.getUserId(), user.getEmail());
            throw new RuntimeException("Invalid email or password");
        }

        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createOrUpdateRefreshToken(user);

        log.info("Login successful for userId: {}, email: {}, role: {}",
                user.getUserId(), user.getEmail(), user.getRole());

        return AuthResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .message("Login successful")
                .build();
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.info("Refresh token flow started");

        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(request.getRefreshToken());
        UserCredential user = refreshToken.getUser();

        if (!user.getIsActive()) {
            log.warn("Refresh token failed because account is deactivated for userId: {}, email: {}",
                    user.getUserId(), user.getEmail());
            throw new RuntimeException("Account is deactivated");
        }

        String accessToken = jwtService.generateToken(user);

        log.info("Access token refreshed successfully for userId: {}, email: {}",
                user.getUserId(), user.getEmail());

        return AuthResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .message("Access token refreshed successfully")
                .build();
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        log.info("Forgot password flow started for email: {}", request.getEmail());

        UserCredential user = authRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Forgot password failed. No account found for email: {}", request.getEmail());
                    return new RuntimeException("No account found with this email");
                });

        passwordResetOtpRepository.deleteByEmail(user.getEmail());
        log.info("Existing OTP entries deleted for email: {}", user.getEmail());

        String otp = String.valueOf((int) ((Math.random() * 900000) + 100000));

        PasswordResetOtp passwordResetOtp = PasswordResetOtp.builder()
                .email(user.getEmail())
                .otp(otp)
                .expiryTime(LocalDateTime.now().plusMinutes(resetOtpExpirationMinutes))
                .used(false)
                .build();

        passwordResetOtpRepository.save(passwordResetOtp);
        log.info("New password reset OTP generated and saved for userId: {}, email: {}",
                user.getUserId(), user.getEmail());

        String message =
                "Dear User,\n\n"
                + "Greetings from HireConnect.\n\n"
                + "We received a request to reset your account password.\n\n"
                + "Your OTP for password reset is: " + otp + "\n"
                + "This OTP is valid for " + resetOtpExpirationMinutes + " minutes.\n\n"
                + "If you did not request this, please ignore this email.\n\n"
                + "Regards,\n"
                + "Support Team\n"
                + "HireConnect";

        NotificationEvent event = NotificationEvent.builder()
                .recipientUserId(user.getUserId())
                .recipientEmail(user.getEmail())
                .title("Password Reset OTP")
                .message(message)
                .type(NotificationType.SYSTEM)
                .sendEmail(true)
                .build();

        notificationEventProducer.sendNotification(event);

        log.info("Password reset notification request sent successfully for userId: {}, email: {}",
                user.getUserId(), user.getEmail());
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        log.info("Reset password flow started for email: {}", request.getEmail());

        // BUG FIX 7a: Validate new password is not blank before doing any DB work
        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            throw new RuntimeException("New password must not be blank");
        }

        UserCredential user = authRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Reset password failed. No account found for email: {}", request.getEmail());
                    return new RuntimeException("No account found with this email");
                });

        PasswordResetOtp passwordResetOtp = passwordResetOtpRepository
                .findTopByEmailAndOtpAndUsedFalseOrderByIdDesc(request.getEmail(), request.getOtp())
                .orElseThrow(() -> {
                    log.warn("Reset password failed due to invalid OTP for email: {}", request.getEmail());
                    return new RuntimeException("Invalid OTP");
                });

        if (passwordResetOtp.getExpiryTime().isBefore(LocalDateTime.now())) {
            log.warn("Reset password failed because OTP expired for email: {}", request.getEmail());
            throw new RuntimeException("OTP has expired");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        authRepository.save(user);

        passwordResetOtp.setUsed(true);
        passwordResetOtpRepository.save(passwordResetOtp);

        // BUG FIX 7b: After password reset, invalidate the existing refresh token.
        // Without this, an attacker who had the old refresh token can still get
        // new access tokens even after the victim resets their password.
        refreshTokenRepository.findByUser(user).ifPresent(rt -> {
            log.info("Invalidating refresh token after password reset for userId: {}", user.getUserId());
            refreshTokenRepository.delete(rt);
        });

        log.info("Password reset successful for userId: {}, email: {}", user.getUserId(), user.getEmail());
    }

    @Override
    public TokenValidationResponse validateToken(String token) {
        log.info("Token validation started");

        try {
            if (token == null || token.isBlank()) {
                log.warn("Token validation failed because token is missing");
                throw new RuntimeException("Token is missing");
            }

            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            boolean valid = jwtService.isTokenValid(token);

            if (!valid) {
                log.warn("Token validation failed because token is invalid or expired");
                throw new RuntimeException("Invalid or expired token");
            }

            Long userId = jwtService.extractUserId(token);
            String email = jwtService.extractEmail(token);
            String role = jwtService.extractRole(token);

            log.info("Token validation successful for userId: {}, email: {}, role: {}", userId, email, role);

            return TokenValidationResponse.builder()
                    .valid(true)
                    .userId(userId)
                    .email(email)
                    .role(Enum.valueOf(Role.class, role))
                    .message("Token is valid")
                    .build();
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return TokenValidationResponse.builder()
                    .valid(false)
                    .message("Invalid or expired token")
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminUserResponse> getAllUsersForAdmin(AuthenticatedUser user) {
        validateAdmin(user);
        return authRepository.findAll().stream()
                .map(this::toAdminUserResponse)
                .toList();
    }

    @Override
    @Transactional
    public AdminUserResponse updateUserActiveStatus(AuthenticatedUser user, Long targetUserId, boolean active) {
        validateAdmin(user);

        UserCredential target = authRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (target.getRole() == Role.ADMIN && !target.getUserId().equals(user.userId())) {
            throw new RuntimeException("Admin accounts can only be managed by themselves");
        }

        target.setIsActive(active);
        UserCredential saved = authRepository.save(target);

        if (!active) {
            refreshTokenRepository.findByUser(saved).ifPresent(refreshTokenRepository::delete);
        }

        return toAdminUserResponse(saved);
    }

    private String buildDefaultFullName(String email) {
        if (email == null || email.isBlank()) {
            return "User";
        }

        String localPart = email.split("@")[0].trim();
        if (localPart.isEmpty()) {
            return "User";
        }

        String normalized = localPart.replace('.', ' ').replace('_', ' ').replace('-', ' ');
        String[] parts = normalized.trim().split("\\s+");
        StringBuilder fullName = new StringBuilder();

        for (String part : parts) {
            if (part.isEmpty()) continue;
            if (fullName.length() > 0) fullName.append(' ');
            fullName.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) fullName.append(part.substring(1));
        }

        return fullName.length() == 0 ? "User" : fullName.toString();
    }

    private String resolveFullName(String fullName, String email) {
        if (fullName == null || fullName.isBlank()) {
            return buildDefaultFullName(email);
        }
        return fullName.trim().replaceAll("\\s+", " ");
    }

    private void validateAdmin(AuthenticatedUser user) {
        if (user == null || user.role() != Role.ADMIN) {
            throw new RuntimeException("Only admins can perform this action");
        }
    }

    private AdminUserResponse toAdminUserResponse(UserCredential user) {
        return AdminUserResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .provider(user.getProvider())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
