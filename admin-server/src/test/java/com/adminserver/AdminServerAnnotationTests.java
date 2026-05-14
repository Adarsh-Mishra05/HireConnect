package com.adminserver;

import static org.junit.jupiter.api.Assertions.assertTrue;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

class AdminServerAnnotationTests {

    @Test
    void shouldBeSpringBootApplication() {
        assertTrue(AdminServerApplication.class.isAnnotationPresent(SpringBootApplication.class));
    }

    @Test
    void shouldEnableAdminServer() {
        assertTrue(AdminServerApplication.class.isAnnotationPresent(EnableAdminServer.class));
    }
}
