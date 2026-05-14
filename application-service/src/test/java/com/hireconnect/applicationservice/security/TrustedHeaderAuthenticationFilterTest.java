package com.hireconnect.applicationservice.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.hireconnect.applicationservice.enums.Role;

class TrustedHeaderAuthenticationFilterTest {

    private final TrustedHeaderAuthenticationFilter filter = new TrustedHeaderAuthenticationFilter();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_SetsAuthentication_WhenHeadersAreValid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Auth-User-Id", "1");
        request.addHeader("X-Auth-User-Email", "candidate@example.com");
        request.addHeader("X-Auth-User-Role", "CANDIDATE");

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof AuthenticatedUser);
        AuthenticatedUser user = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        assertEquals(1L, user.getUserId());
        assertEquals(Role.CANDIDATE, user.getRole());
    }

    @Test
    void doFilterInternal_ClearsContext_WhenRoleHeaderIsInvalid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Auth-User-Id", "1");
        request.addHeader("X-Auth-User-Email", "candidate@example.com");
        request.addHeader("X-Auth-User-Role", "NOT_A_ROLE");

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_DoesNotOverrideExistingAuthentication() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("existing", null));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Auth-User-Id", "1");
        request.addHeader("X-Auth-User-Email", "candidate@example.com");
        request.addHeader("X-Auth-User-Role", "CANDIDATE");

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertEquals("existing", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }
}
