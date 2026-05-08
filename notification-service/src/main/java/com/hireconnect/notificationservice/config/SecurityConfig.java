package com.hireconnect.notificationservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.hireconnect.notificationservice.security.TrustedHeaderAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    private final TrustedHeaderAuthenticationFilter trustedHeaderAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("Configuring security filter chain for notification-service");

        http
                // CORS is handled at API Gateway. Avoid adding service-level CORS headers
                // to prevent duplicate Access-Control-Allow-Origin values on proxied responses.
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                	    .authenticationEntryPoint((request, response, authException) -> {
                	        logger.warn("Unauthorized access attempt for path: {}", request.getRequestURI());
                	        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                	        response.setContentType("application/json");
                	        response.getWriter().write("{\"message\":\"Unauthorized\"}");
                	    })
                	)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/actuator/**"
                        ).permitAll()
                        // Internal service-to-service endpoints should not require end-user auth headers.
                        .requestMatchers("/internal/notifications/**").permitAll()
                        .requestMatchers("/api/notifications/**").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(trustedHeaderAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
