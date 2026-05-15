package com.hireconnect.jobservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

@Component
public class RedisConnectionVerifier implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(RedisConnectionVerifier.class);

    private final RedisConnectionFactory redisConnectionFactory;

    public RedisConnectionVerifier(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    @Override
    public void run(String... args) {
        try (var connection = redisConnectionFactory.getConnection()) {
            String pong = connection.ping();
            log.info("Redis connectivity check successful. PING response={}", pong);
        } catch (Exception ex) {
            log.error("Redis connectivity check failed: {}", ex.getMessage(), ex);
        }
    }
}
