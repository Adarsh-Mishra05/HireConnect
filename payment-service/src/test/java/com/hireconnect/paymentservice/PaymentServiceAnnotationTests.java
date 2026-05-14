package com.hireconnect.paymentservice;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

class PaymentServiceAnnotationTests {

    @Test
    void shouldBeSpringBootApplication() {
        assertTrue(PaymentServiceApplication.class.isAnnotationPresent(SpringBootApplication.class));
    }

    @Test
    void shouldEnableFeignClients() {
        assertTrue(PaymentServiceApplication.class.isAnnotationPresent(EnableFeignClients.class));
    }
}
