package com.example.common.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendCampaignEvent {
    private String campaignId;
    private String userId;
    private String subject;
    private String htmlContent;
    private String textContent;
    private List<RecipientPayload> recipients;
}
