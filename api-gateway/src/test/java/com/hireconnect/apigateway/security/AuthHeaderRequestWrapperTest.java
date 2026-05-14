package com.hireconnect.apigateway.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

class AuthHeaderRequestWrapperTest {

    private HttpServletRequest original;
    private AuthHeaderRequestWrapper wrapper;

    @BeforeEach
    void setUp() throws IOException {
        original = mock(HttpServletRequest.class);
        when(original.getHeaderNames()).thenReturn(Collections.enumeration(List.of("X-Auth-User-Id", "X-Auth-User-Email", "Other-Header")));
        when(original.getHeader("Other-Header")).thenReturn("other-value");
        when(original.getHeaders("Other-Header")).thenReturn(Collections.enumeration(List.of("other-value")));
        when(original.getInputStream()).thenReturn(createServletInputStream("hello"));

        wrapper = new AuthHeaderRequestWrapper(original, 99L, "user@example.com", "ADMIN");
    }

    @Test
    void getHeaderShouldReturnInjectedValues() {
        assertEquals("99", wrapper.getHeader("X-Auth-User-Id"));
        assertEquals("user@example.com", wrapper.getHeader("X-Auth-User-Email"));
        assertEquals("ADMIN", wrapper.getHeader("X-Auth-User-Role"));
        assertEquals("other-value", wrapper.getHeader("Other-Header"));
    }

    @Test
    void getHeadersShouldReturnInjectedValuesAndFilterTrustedHeaders() {
        Enumeration<String> userIdHeaders = wrapper.getHeaders("X-Auth-User-Id");
        assertTrue(userIdHeaders.hasMoreElements());
        assertEquals("99", userIdHeaders.nextElement());

        Enumeration<String> emailHeaders = wrapper.getHeaders("X-Auth-User-Email");
        assertTrue(emailHeaders.hasMoreElements());
        assertEquals("user@example.com", emailHeaders.nextElement());
    }

    @Test
    void getHeaderNamesShouldIncludeInjectedValuesAndExcludeOriginalTrustedHeaders() {
        List<String> names = Collections.list(wrapper.getHeaderNames());
        assertTrue(names.contains("X-Auth-User-Id"));
        assertTrue(names.contains("X-Auth-User-Email"));
        assertTrue(names.contains("X-Auth-User-Role"));
        assertTrue(names.contains("Other-Header"));
    }

    @Test
    void getInputStreamShouldWrapOriginalStream() throws IOException {
        ServletInputStream inputStream = wrapper.getInputStream();
        byte[] buffer = new byte[5];
        int read = inputStream.read(buffer);

        assertEquals(5, read);
        assertEquals("hello", new String(buffer));
        assertFalse(inputStream.isFinished());
        assertTrue(inputStream.isReady());
    }

    private ServletInputStream createServletInputStream(String content) {
        ByteArrayInputStream delegate = new ByteArrayInputStream(content.getBytes());
        return new ServletInputStream() {
            private boolean finished;

            @Override
            public int read() throws IOException {
                int value = delegate.read();
                if (value == -1) {
                    finished = true;
                }
                return value;
            }

            @Override
            public boolean isFinished() {
                return finished;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(jakarta.servlet.ReadListener listener) {
                // no-op
            }
        };
    }
}
