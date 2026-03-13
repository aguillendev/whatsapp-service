package com.example.whatsapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<Map<String, Object>> handleHttpClientError(HttpClientErrorException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "WhatsApp API Client Error");
        body.put("details", ex.getResponseBodyAsString());
        body.put("status", ex.getStatusCode().value());

        return new ResponseEntity<>(body, ex.getStatusCode());
    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<Map<String, Object>> handleHttpServerError(HttpServerErrorException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "WhatsApp API Server Error");
        body.put("details", ex.getResponseBodyAsString());
        body.put("status", ex.getStatusCode().value());

        return new ResponseEntity<>(body, ex.getStatusCode());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Internal Server Error");
        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}