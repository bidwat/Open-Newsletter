package com.example.core_service.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class UpdateCampaignRequest {
    private List<Integer> mailingListIds;
    private String name;
    private String subject;
    private String htmlContent;
    private String textContent;
    private List<Integer> excludedContactIds;
}
