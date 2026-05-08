package com.hireconnect.apigateway.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
public class GatewayJwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private PublicEndpointService publicEndpointService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private GatewayJwtAuthenticationFilter filter;

    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        responseWriter = new StringWriter();
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getMethod()).thenReturn("GET");
    }

    @Test
    void doFilterInternal_OptionsRequest_SkipsJwtValidation() throws Exception {
        when(request.getMethod()).thenReturn("OPTIONS");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtService, never()).isTokenValid(anyString());
    }

    @Test
    void doFilterInternal_MissingAuthorizationHeader_ReturnsUnauthorized() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        filter.doFilterInternal(request, response, filterChain);

        verify(response, times(1)).setStatus(HttpStatus.UNAUTHORIZED.value());
        assertTrue(responseWriter.toString().contains("Missing or invalid Authorization header"));
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void doFilterInternal_InvalidToken_ReturnsUnauthorized() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid_token");
        when(jwtService.isTokenValid("invalid_token")).thenReturn(false);
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        filter.doFilterInternal(request, response, filterChain);

        verify(response, times(1)).setStatus(HttpStatus.UNAUTHORIZED.value());
        assertTrue(responseWriter.toString().contains("Invalid or expired token"));
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void doFilterInternal_ValidToken_ContinuesFilterChain() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid_token");
        when(jwtService.isTokenValid("valid_token")).thenReturn(true);
        when(jwtService.extractUserId("valid_token")).thenReturn(1L);
        when(jwtService.extractEmail("valid_token")).thenReturn("test@example.com");
        when(jwtService.extractRole("valid_token")).thenReturn("CANDIDATE");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(any(AuthHeaderRequestWrapper.class), eq(response));
    }
}
