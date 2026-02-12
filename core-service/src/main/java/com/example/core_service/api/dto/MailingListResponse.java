package com.example.core_service.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class MailingListResponse {
    private Integer id;
    private String name;
    private String description;
    private boolean hidden;
    private LocalDateTime createdAt;
}
