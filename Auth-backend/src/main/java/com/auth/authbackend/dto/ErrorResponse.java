package com.auth.authbackend.dto;

import org.springframework.http.HttpStatus;

public record ErrorResponse(
        String message,
        HttpStatus status,
        int statusCode
) {
    public ErrorResponse(String message, HttpStatus status) {
        this(message, status, status.value());
    }
}
