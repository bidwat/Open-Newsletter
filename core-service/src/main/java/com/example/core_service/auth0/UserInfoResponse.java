package com.example.core_service.auth0;

public record UserInfoResponse(
        String sub,
        String email,
        Boolean email_verified,
        String name
) {}
