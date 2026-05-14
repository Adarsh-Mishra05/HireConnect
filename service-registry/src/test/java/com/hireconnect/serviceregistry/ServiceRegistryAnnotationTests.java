package com.hireconnect.serviceregistry;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

class ServiceRegistryAnnotationTests {

    @Test
    void shouldBeSpringBootApplication() {
        assertTrue(ServiceRegistryApplication.class.isAnnotationPresent(SpringBootApplication.class));
    }

    @Test
    void shouldEnableEurekaServer() {
        assertTrue(ServiceRegistryApplication.class.isAnnotationPresent(EnableEurekaServer.class));
    }
}
