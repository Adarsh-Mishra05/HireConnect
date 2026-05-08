package com.hireconnect.profileservice.config;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

	private final TrustedHeaderAuthenticationFilter trustedHeaderAuthenticationFilter;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http

				.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					response.setContentType("application/json");
					response.getWriter().write("{\"message\":\"Unauthorized\"}");
				}))
				.authorizeHttpRequests(
						auth -> auth
								.requestMatchers(
										"/v3/api-docs/**",
										"/swagger-ui/**",
										"/swagger-ui.html",
										"/api/profiles/internal/**"
								).permitAll()
								.requestMatchers("/api/profiles/**").authenticated()
								.anyRequest().permitAll())
				.addFilterBefore(trustedHeaderAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

}