package com.example.core_service.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class CampaignResponse {
    private Integer id;
    private List<MailingListSummaryResponse> mailingLists;
    private String name;
    private String subject;
    private String htmlContent;
    private String textContent;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private List<Integer> excludedContactIds;
}
