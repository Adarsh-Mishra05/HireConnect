package com.hireconnect.auth.controller;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hireconnect.auth.dto.request.ForgotPasswordRequest;
import com.hireconnect.auth.dto.request.LoginRequest;
import com.hireconnect.auth.dto.request.RefreshTokenRequest;
import com.hireconnect.auth.dto.request.RegisterRequest;
import com.hireconnect.auth.dto.request.ResetPasswordRequest;
import com.hireconnect.auth.dto.response.AuthResponse;
import com.hireconnect.auth.dto.response.TokenValidationResponse;
import com.hireconnect.auth.entity.Role;
import com.hireconnect.auth.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private static final String OAUTH_SELECTED_ROLE = "OAUTH_SELECTED_ROLE";

    private final AuthService authService;
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Value("${auth.oauth-gateway-base-url:http://localhost:8080}")
    private String oauthGatewayBaseUrl;

    @PostMapping("/request-otp")
    public ResponseEntity<String> requestOtp(@Validated @RequestBody com.hireconnect.auth.dto.request.OtpRequest request) {
        authService.requestOtp(request);
        return new ResponseEntity<>("OTP sent successfully to " + request.getEmail(), HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Validated @RequestBody RegisterRequest request) {
        log.info("Register request received for email: {}, role: {}", request.getEmail(), request.getRole());
        return new ResponseEntity<>(authService.register(request), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Validated @RequestBody LoginRequest request) {
        log.info("Login request received for email: {}", request.getEmail());
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Validated @RequestBody RefreshTokenRequest request) {
        log.info("Refresh token request received");
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Validated @RequestBody ForgotPasswordRequest request) {
        log.info("Forgot password request received for email: {}", request.getEmail());
        authService.forgotPassword(request);
        return ResponseEntity.ok("OTP sent successfully to your email");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Validated @RequestBody ResetPasswordRequest request) {
        log.info("Reset password request received for email: {}", request.getEmail());
        authService.resetPassword(request);
        return ResponseEntity.ok("Password reset successfully");
    }

    @GetMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validateToken(
            @RequestHeader("Authorization") String authHeader) {
        log.info("Token validation request received");
        return ResponseEntity.ok(authService.validateToken(authHeader));
    }

    @GetMapping("/oauth2/authorize/google")
    public void authorizeGoogle(
            @RequestParam(value = "role", required = false) String roleParam,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        log.info("Google OAuth authorization requested with role: {}", roleParam);

        Role selectedRole = null;
        if (roleParam != null && !roleParam.isBlank()) {
            try {
                selectedRole = Role.valueOf(roleParam.toUpperCase());
            } catch (Exception ex) {
                log.warn("Invalid role received for Google OAuth authorization: {}", roleParam);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid role");
                return;
            }

            if (selectedRole != Role.CANDIDATE && selectedRole != Role.RECRUITER) {
                log.warn("Unsupported role received for Google OAuth authorization: {}", selectedRole);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Only CANDIDATE or RECRUITER allowed");
                return;
            }
        }

        if (selectedRole != null) {
            request.getSession(true).setAttribute(OAUTH_SELECTED_ROLE, selectedRole.name());
            log.info("Redirecting to Google OAuth flow for role: {}", selectedRole);
        } else {
            request.getSession(true).removeAttribute(OAUTH_SELECTED_ROLE);
            log.info("Redirecting to Google OAuth flow without role (existing user sign-in path)");
        }
        redirectStrategy.sendRedirect(request, response, buildGatewayUrl("/oauth2/authorization/google"));
    }

    @GetMapping("/oauth2/authorize/github")
    public void authorizeGitHub(
            @RequestParam(value = "role", required = false) String roleParam,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        log.info("GitHub OAuth authorization requested with role: {}", roleParam);

        Role selectedRole = null;
        if (roleParam != null && !roleParam.isBlank()) {
            try {
                selectedRole = Role.valueOf(roleParam.toUpperCase());
            } catch (Exception ex) {
                log.warn("Invalid role received for GitHub OAuth authorization: {}", roleParam);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid role");
                return;
            }

            if (selectedRole != Role.CANDIDATE && selectedRole != Role.RECRUITER) {
                log.warn("Unsupported role received for GitHub OAuth authorization: {}", selectedRole);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Only CANDIDATE or RECRUITER allowed");
                return;
            }
        }

        if (selectedRole != null) {
            request.getSession(true).setAttribute(OAUTH_SELECTED_ROLE, selectedRole.name());
            log.info("Redirecting to GitHub OAuth flow for role: {}", selectedRole);
        } else {
            request.getSession(true).removeAttribute(OAUTH_SELECTED_ROLE);
            log.info("Redirecting to GitHub OAuth flow without role (existing user sign-in path)");
        }
        redirectStrategy.sendRedirect(request, response, buildGatewayUrl("/oauth2/authorization/github"));
    }

    private String buildGatewayUrl(String path) {
        String base = oauthGatewayBaseUrl != null ? oauthGatewayBaseUrl.trim() : "http://localhost:8080";
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + path;
    }

}
  
