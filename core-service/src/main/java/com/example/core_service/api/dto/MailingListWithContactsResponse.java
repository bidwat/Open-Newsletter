package com.example.core_service.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class MailingListWithContactsResponse {
    private Integer id;
    private String name;
    private String description;
    private boolean hidden;
    private LocalDateTime createdAt;
    private List<ContactResponse> contacts;
}
