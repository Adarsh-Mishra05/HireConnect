package com.hireconnect.jobservice.exception;

public class UnauthorizedJobAccessException extends RuntimeException {
    public UnauthorizedJobAccessException(String message) {
        super(message);
    }
}