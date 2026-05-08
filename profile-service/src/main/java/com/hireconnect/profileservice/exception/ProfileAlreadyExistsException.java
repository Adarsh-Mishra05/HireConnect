package com.hireconnect.profileservice.exception;

public class ProfileAlreadyExistsException extends RuntimeException {

    public ProfileAlreadyExistsException(String message) {
        super(message);
    }
}