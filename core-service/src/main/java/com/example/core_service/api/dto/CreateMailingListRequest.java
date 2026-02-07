package com.example.core_service.api.dto;

import lombok.Data;

@Data
public class CreateMailingListRequest {
    private String name;
    private String description;
}
