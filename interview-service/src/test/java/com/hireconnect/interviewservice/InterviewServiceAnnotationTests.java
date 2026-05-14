package com.hireconnect.interviewservice;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

class InterviewServiceAnnotationTests {

    @Test
    void shouldBeSpringBootApplication() {
        assertTrue(InterviewServiceApplication.class.isAnnotationPresent(SpringBootApplication.class));
    }

    @Test
    void shouldEnableFeignClients() {
        assertTrue(InterviewServiceApplication.class.isAnnotationPresent(EnableFeignClients.class));
    }
}
