package com.example.core_service.api.dto;

import lombok.Data;

@Data
public class AddContactRequest {
    private String email;
    private String firstName;
    private String lastName;
}
