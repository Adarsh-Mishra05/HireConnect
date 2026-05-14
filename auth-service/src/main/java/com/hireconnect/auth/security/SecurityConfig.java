package com.hireconnect.auth.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.hireconnect.auth.client.NotificationServiceClient;
import com.hireconnect.auth.repository.AuthRepository;
import com.hireconnect.auth.service.RefreshTokenService;

@Configuration
public class SecurityConfig {

    @Value("${auth.oauth-success-redirect-url}")
    private String successRedirectUrl;

    @Value("${auth.oauth-failure-redirect-url}")
    private String failureRedirectUrl;

    @Bean
    public OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler(
            AuthRepository authRepository,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            PasswordEncoder passwordEncoder,
            NotificationServiceClient notificationServiceClient
    ) {
        // to the correct failure page instead of the success page with ?error=
        return new OAuth2AuthenticationSuccessHandler(
                authRepository,
                jwtService,
                refreshTokenService,
                passwordEncoder,
                notificationServiceClient,
                successRedirectUrl,
                failureRedirectUrl
        );
    }

    @Bean
    public OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler() {
        return new OAuth2AuthenticationFailureHandler(failureRedirectUrl);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler,
            OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler,
            TrustedHeaderAuthenticationFilter trustedHeaderAuthenticationFilter
    ) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/api/v1/auth/**",
                        "/oauth2/**",
                        "/login/oauth2/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/actuator/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth -> oauth
                .successHandler(oAuth2AuthenticationSuccessHandler)
                .failureHandler(oAuth2AuthenticationFailureHandler)
            )
            .addFilterBefore(trustedHeaderAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
