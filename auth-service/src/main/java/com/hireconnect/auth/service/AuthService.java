package com.hireconnect.auth.service;

import java.util.List;

import com.hireconnect.auth.dto.request.ForgotPasswordRequest;
import com.hireconnect.auth.dto.request.LoginRequest;
import com.hireconnect.auth.dto.request.OtpRequest;
import com.hireconnect.auth.dto.request.RefreshTokenRequest;
import com.hireconnect.auth.dto.request.RegisterRequest;
import com.hireconnect.auth.dto.request.ResetPasswordRequest;
import com.hireconnect.auth.dto.response.AdminUserResponse;
import com.hireconnect.auth.dto.response.AuthResponse;
import com.hireconnect.auth.dto.response.TokenValidationResponse;
import com.hireconnect.auth.security.AuthenticatedUser;

// Defines the contract for user registration, credential-based login, JWT refresh token exchange,
// OTP-based forgot/reset password flow, and token validation called by the API Gateway.
public interface AuthService {

    void requestOtp(OtpRequest request);

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);
    
    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    TokenValidationResponse validateToken(String token);

    List<AdminUserResponse> getAllUsersForAdmin(AuthenticatedUser user);

    AdminUserResponse updateUserActiveStatus(AuthenticatedUser user, Long targetUserId, boolean active);
}
