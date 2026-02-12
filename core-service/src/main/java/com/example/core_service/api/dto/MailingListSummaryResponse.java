package com.example.core_service.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MailingListSummaryResponse {
    private Integer id;
    private String name;
    private boolean hidden;
}
