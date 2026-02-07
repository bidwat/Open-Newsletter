package com.example.core_service.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ContactResponse {
    private Integer id;
    private String email;
    private String firstName;
    private String lastName;
}
