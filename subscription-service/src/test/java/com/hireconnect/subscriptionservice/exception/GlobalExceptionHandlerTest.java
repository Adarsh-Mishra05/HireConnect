package com.hireconnect.subscriptionservice.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandleNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Not found");
        ResponseEntity<Map<String, Object>> response = handler.handleNotFound(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Not found", response.getBody().get("message"));
    }

    @Test
    void testHandleBadRequest_Exception() {
        BadRequestException ex = new BadRequestException("Bad request");
        ResponseEntity<Map<String, Object>> response = handler.handleBadRequest(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Bad request", response.getBody().get("message"));
    }

    @Test
    void testHandleBadRequest_Validation() {
        org.springframework.validation.BeanPropertyBindingResult bindingResult = 
            new org.springframework.validation.BeanPropertyBindingResult(new Object(), "object");
        bindingResult.addError(new FieldError("object", "field", "Invalid field"));
        
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<Map<String, Object>> response = handler.handleBadRequest(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid field", response.getBody().get("message"));
    }

    @Test
    void testHandleUnauthorized() {
        UnauthorizedException ex = new UnauthorizedException("Unauthorized");
        ResponseEntity<Map<String, Object>> response = handler.handleUnauthorized(ex);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Unauthorized", response.getBody().get("message"));
    }

    @Test
    void testHandleGeneric() {
        Exception ex = new Exception("Generic error");
        ResponseEntity<Map<String, Object>> response = handler.handleGeneric(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal server error", response.getBody().get("message"));
    }
}
