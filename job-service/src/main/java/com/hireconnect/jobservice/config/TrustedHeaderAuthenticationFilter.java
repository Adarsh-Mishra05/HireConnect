package com.hireconnect.jobservice.config;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.hireconnect.jobservice.entity.Role;
import com.hireconnect.jobservice.security.AuthenticatedUser;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TrustedHeaderAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String userIdHeader = request.getHeader("X-Auth-User-Id");
        String emailHeader = request.getHeader("X-Auth-User-Email");
        String roleHeader = request.getHeader("X-Auth-User-Role");

        if (userIdHeader != null && emailHeader != null && roleHeader != null
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Long userId = Long.valueOf(userIdHeader);
                Role role = Role.valueOf(roleHeader);

                AuthenticatedUser authenticatedUser =
                        new AuthenticatedUser(userId, emailHeader, role);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                authenticatedUser,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception ex) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}