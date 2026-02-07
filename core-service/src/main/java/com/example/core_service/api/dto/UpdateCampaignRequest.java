package com.example.core_service.api.dto;

import lombok.Data;

@Data
public class UpdateCampaignRequest {
    private String name;
    private String subject;
    private String htmlContent;
    private String textContent;
}
