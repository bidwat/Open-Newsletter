package com.example.core_service.api.dto;

import lombok.Data;

@Data
public class CreateCampaignRequest {
    private Integer mailingListId;
    private String name;
    private String subject;
    private String htmlContent;
    private String textContent;
}
