package com.hireconnect.apigateway.security;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class GatewayJwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final PublicEndpointService publicEndpointService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        boolean isPublic = publicEndpointService.isPublic(request);
        if (isPublic) {
            log.debug("Skipping JWT filter for public endpoint: {} {}", request.getMethod(), request.getRequestURI());
        }
        return isPublic;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        String method = request.getMethod();
        log.info("Processing gateway request: {} {}", method, requestPath);

        if ("OPTIONS".equalsIgnoreCase(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Unauthorized request to {} {} - missing or invalid Authorization header", method, requestPath);
            // BUG 1 FIX: Only clear context on failure, not in finally block.
            // Clearing in finally{} caused thread-pool reuse to see empty context
            // on the next request, stripping the Authorization header.
            SecurityContextHolder.clearContext();
            writeUnauthorized(response, "Missing or invalid Authorization header");
            return;
        }

        String token = authHeader.substring(7);

        final Long userId;
        final String email;
        final String role;
        try {
            if (!jwtService.isTokenValid(token)) {
                log.warn("Unauthorized request to {} {} - token is invalid or expired", method, requestPath);
                SecurityContextHolder.clearContext();
                writeUnauthorized(response, "Invalid or expired token");
                return;
            }

            userId = jwtService.extractUserId(token);
            email = jwtService.extractEmail(token);
            role = jwtService.extractRole(token);
        } catch (Exception ex) {
            // Only JWT parsing/validation problems should map to 401.
            log.error("JWT parsing/validation failed for {} {}: {}", method, requestPath, ex.getMessage());
            SecurityContextHolder.clearContext();
            writeUnauthorized(response, "Token validation failed");
            return;
        }

        log.info("JWT validated for userId: {}, email: {}, role: {}, path: {}", userId, email, role, requestPath);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        AuthHeaderRequestWrapper wrappedRequest =
                new AuthHeaderRequestWrapper(request, userId, email, role);

        filterChain.doFilter(wrappedRequest, response);
        // BUG 1 FIX: Do NOT call SecurityContextHolder.clearContext() here.
        // Spring Security's SecurityContextHolderFilter handles cleanup after
        // the full filter chain completes. Calling it here prematurely clears
        // the context mid-request and corrupts subsequent requests on reused threads.
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}
