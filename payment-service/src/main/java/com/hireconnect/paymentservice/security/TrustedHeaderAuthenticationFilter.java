package com.hireconnect.paymentservice.security;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.hireconnect.paymentservice.enums.Role;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TrustedHeaderAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(TrustedHeaderAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String userIdHeader = request.getHeader("X-Auth-User-Id");
        String emailHeader = request.getHeader("X-Auth-User-Email");
        String roleHeader = request.getHeader("X-Auth-User-Role");

        if (userIdHeader != null && emailHeader != null && roleHeader != null
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                logger.debug("Trusted auth headers found for request URI={}", request.getRequestURI());
                Long userId = Long.valueOf(userIdHeader);
                Role role = Role.valueOf(roleHeader);

                AuthenticatedUser user = new AuthenticatedUser(userId, emailHeader, role);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.debug("Authentication set from trusted headers for userId={}, role={}", userId, role);
            } catch (Exception ex) {
                logger.warn("Failed to authenticate request from trusted headers for URI={}", request.getRequestURI(), ex);
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}