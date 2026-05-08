package com.hireconnect.auth.security;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2AuthenticationFailureHandler.class);

    private final String failureRedirectUrl;

    public OAuth2AuthenticationFailureHandler(String failureRedirectUrl) {
        this.failureRedirectUrl = failureRedirectUrl;
    }

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {

        String message = exception.getMessage() != null
                ? exception.getMessage()
                : "Google login failed";

        log.error("Google OAuth authentication failed: {}", message, exception);

        String separator = failureRedirectUrl.contains("?") ? "&" : "?";
        String redirectUrl = failureRedirectUrl + separator + "error="
                + URLEncoder.encode(message, StandardCharsets.UTF_8);

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
