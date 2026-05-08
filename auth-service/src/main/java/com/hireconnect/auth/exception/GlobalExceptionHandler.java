package com.hireconnect.auth.exception;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // BUG FIX 1: Was returning 400 for ALL RuntimeExceptions, including refresh
    // token failures ("Invalid refresh token", "Refresh token expired").
    // The frontend interceptor only retries on 401 — so a 400 from a failed
    // refresh would be treated as a final error and immediately clear the session.
    // Now refresh-token errors return 401 so the frontend handles them correctly,
    // and other runtime errors return 400 as before.
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime exception occurred: {}", ex.getMessage(), ex);

        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());

        String msg = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";

        if (msg.contains("invalid refresh token")
                || msg.contains("refresh token expired")
                || msg.contains("refresh token")) {
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED); // 401
        }

        if (msg.contains("invalid email or password")
                || msg.contains("invalid credentials")) {
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED); // 401
        }

        if (msg.contains("account is deactivated")) {
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN); // 403
        }

        if (msg.contains("already registered") || msg.contains("already exists")) {
            return new ResponseEntity<>(response, HttpStatus.CONFLICT); // 409
        }

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST); // 400
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex) {
        log.error("Validation exception occurred: {}", ex.getMessage(), ex);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Validation failed");
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
