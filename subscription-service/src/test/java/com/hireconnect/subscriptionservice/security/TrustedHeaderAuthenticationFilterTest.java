package com.hireconnect.subscriptionservice.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;

import com.hireconnect.subscriptionservice.enums.Role;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class TrustedHeaderAuthenticationFilterTest {

    private TrustedHeaderAuthenticationFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new TrustedHeaderAuthenticationFilter();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);
        SecurityContextHolder.clearContext();
    }

    @Test
    void testDoFilterInternal_Success() throws ServletException, IOException {
        when(request.getHeader("X-Auth-User-Id")).thenReturn("1");
        when(request.getHeader("X-Auth-User-Email")).thenReturn("test@test.com");
        when(request.getHeader("X-Auth-User-Role")).thenReturn("RECRUITER");

        filter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        AuthenticatedUser user = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        assertEquals(1L, user.userId());
        assertEquals("test@test.com", user.email());
        assertEquals(Role.RECRUITER, user.role());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_MissingHeaders() throws ServletException, IOException {
        when(request.getHeader("X-Auth-User-Id")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_InvalidRole() throws ServletException, IOException {
        when(request.getHeader("X-Auth-User-Id")).thenReturn("1");
        when(request.getHeader("X-Auth-User-Email")).thenReturn("test@test.com");
        when(request.getHeader("X-Auth-User-Role")).thenReturn("INVALID_ROLE");

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}
