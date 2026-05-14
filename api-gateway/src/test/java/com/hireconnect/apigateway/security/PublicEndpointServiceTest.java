package com.hireconnect.apigateway.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PublicEndpointServiceTest {

    private PublicEndpointService service;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        service = new PublicEndpointService();
        request = mock(HttpServletRequest.class);
    }

    @Test
    void shouldMarkAuthEndpointsPublic() {
        when(request.getRequestURI()).thenReturn("/api/v1/auth/login");
        when(request.getMethod()).thenReturn("POST");

        assertTrue(service.isPublic(request));
    }

    @Test
    void shouldMarkSwaggerAndDocsPublic() {
        when(request.getRequestURI()).thenReturn("/swagger-ui/index.html");
        when(request.getMethod()).thenReturn("GET");

        assertTrue(service.isPublic(request));
    }

    @Test
    void shouldMarkJobSearchPathPublicForGet() {
        when(request.getRequestURI()).thenReturn("/api/jobs/search/developer");
        when(request.getMethod()).thenReturn("GET");

        assertTrue(service.isPublic(request));
    }

    @Test
    void shouldMarkAggregatePathsPublic() {
        when(request.getRequestURI()).thenReturn("/aggregate/payment/v3/api-docs");
        when(request.getMethod()).thenReturn("GET");

        assertTrue(service.isPublic(request));
    }

    @Test
    void shouldMarkProtectedPathsAsNotPublic() {
        when(request.getRequestURI()).thenReturn("/api/private/dashboard");
        when(request.getMethod()).thenReturn("GET");

        assertFalse(service.isPublic(request));
    }
}
