package com.hireconnect.auth.security;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import com.hireconnect.auth.entity.AuthProvider;
import com.hireconnect.auth.entity.RefreshToken;
import com.hireconnect.auth.entity.Role;
import com.hireconnect.auth.entity.UserCredential;
import com.hireconnect.auth.repository.AuthRepository;
import com.hireconnect.auth.service.RefreshTokenService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler.class);
    private static final String OAUTH_SELECTED_ROLE = "OAUTH_SELECTED_ROLE";

    private final AuthRepository authRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final String successRedirectUrl;
    // BUG FIX 6: Was using successRedirectUrl for failure redirects too.
    // Added failureRedirectUrl so OAuth errors send users to the correct page.
    private final String failureRedirectUrl;

    public OAuth2AuthenticationSuccessHandler(
            AuthRepository authRepository,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            PasswordEncoder passwordEncoder,
            String successRedirectUrl,
            String failureRedirectUrl
    ) {
        this.authRepository = authRepository;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.passwordEncoder = passwordEncoder;
        this.successRedirectUrl = successRedirectUrl;
        this.failureRedirectUrl = failureRedirectUrl;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        log.info("OAuth2 authentication success handler invoked");

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String registrationId = ((org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
        log.info("OAuth provider: {}", registrationId);

        String email = null;
        String fullName = null;
        AuthProvider provider = null;

        if ("google".equals(registrationId)) {
            provider = AuthProvider.GOOGLE;
            email = oAuth2User.getAttribute("email");
            fullName = oAuth2User.getAttribute("name");
            if (email == null || email.isBlank()) {
                log.warn("Google OAuth failed because email was not found in Google account");
                response.sendRedirect(buildFailureUrl("Email not found from Google account"));
                return;
            }
        } else if ("github".equals(registrationId)) {
            provider = AuthProvider.GITHUB;
            email = oAuth2User.getAttribute("email");
            if (email == null || email.isBlank()) {
                email = oAuth2User.getAttribute("login") + "@github.local";
            }
            fullName = oAuth2User.getAttribute("name");
            if (fullName == null || fullName.isBlank()) {
                fullName = oAuth2User.getAttribute("login");
            }
        } else {
            log.warn("Unsupported OAuth provider: {}", registrationId);
            response.sendRedirect(buildFailureUrl("Unsupported OAuth provider"));
            return;
        }

        if (email == null || email.isBlank()) {
            log.warn("OAuth failed because email was not found for provider: {}", registrationId);
            response.sendRedirect(buildFailureUrl("Email not found from " + registrationId + " account"));
            return;
        }

        String selectedRoleValue = (String) request.getSession().getAttribute(OAUTH_SELECTED_ROLE);
        Role selectedRole = null;

        if (selectedRoleValue != null) {
            try {
                selectedRole = Role.valueOf(selectedRoleValue);
            } catch (Exception ignored) {
                log.warn("Invalid selected role found in session for email: {}", email);
            }
        }

        UserCredential existingUser = authRepository.findByEmail(email).orElse(null);

        UserCredential user;
        if (existingUser != null) {
            user = existingUser;
            log.info("Existing {} OAuth user found for userId: {}, email: {}, role: {}",
                    registrationId.toUpperCase(), user.getUserId(), user.getEmail(), user.getRole());
        } else {
            // If role is not provided during first social sign-in, default to CANDIDATE.
            // Explicit role selection (CANDIDATE/RECRUITER) is still honored when present.
            if (selectedRole == null) {
                selectedRole = Role.CANDIDATE;
                log.info("{} OAuth role not provided for new user {}. Defaulting role to CANDIDATE.",
                        registrationId.toUpperCase(), email);
            } else if (selectedRole != Role.CANDIDATE && selectedRole != Role.RECRUITER) {
                log.warn("{} OAuth failed because role selection is invalid for email: {}", registrationId.toUpperCase(), email);
                response.sendRedirect(buildFailureUrl("Role selection invalid"));
                return;
            }

            user = UserCredential.builder()
                    .email(email)
                    .fullName(resolveFullName(fullName, email))
                    .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .role(selectedRole)
                    .provider(provider)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            user = authRepository.save(user);
            log.info("New {} OAuth user created with userId: {}, email: {}, role: {}",
                    registrationId.toUpperCase(), user.getUserId(), user.getEmail(), user.getRole());
        }

        request.getSession().removeAttribute(OAUTH_SELECTED_ROLE);

        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createOrUpdateRefreshToken(user);

        String redirectUrl = successRedirectUrl
                + "?accessToken=" + encode(accessToken)
                + "&refreshToken=" + encode(refreshToken.getToken())
                + "&email=" + encode(user.getEmail())
                + "&fullName=" + encode(user.getFullName())
                + "&role=" + encode(user.getRole().name())
                + "&userId=" + user.getUserId();

        log.info("Redirecting {} OAuth user to success URL for userId: {}", registrationId.toUpperCase(), user.getUserId());
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    // BUG FIX 6: Now correctly redirects to failureRedirectUrl, not successRedirectUrl
    private String buildFailureUrl(String message) {
        String separator = failureRedirectUrl.contains("?") ? "&" : "?";
        return failureRedirectUrl + separator + "oauthError=" + encode(message);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String resolveFullName(String googleName, String email) {
        if (googleName != null && !googleName.isBlank()) {
            return googleName.trim();
        }
        if (email == null || email.isBlank()) {
            return "User";
        }
        String localPart = email.split("@")[0].trim();
        return localPart.isEmpty() ? "User" : localPart;
    }
}
