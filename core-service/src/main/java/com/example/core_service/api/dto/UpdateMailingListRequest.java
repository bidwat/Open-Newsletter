package com.example.core_service.api.dto;

import lombok.Data;

@Data
public class UpdateMailingListRequest {
    private String name;
    private String description;
}
