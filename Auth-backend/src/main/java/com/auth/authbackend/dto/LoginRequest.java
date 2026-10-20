package com.auth.authbackend.dto;

public record LoginRequest(
        String email,
        String password
) {
}