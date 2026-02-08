package com.example.core_service.api.dto;

import lombok.Data;

@Data
public class UpdateContactRequest {
    private String firstName;
    private String lastName;
    private String email;
}
