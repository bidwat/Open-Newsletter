package com.example.common.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryStatusEvent {
    private String campaignId;
    private String userId;
    private String contactId;
    private String email;
    private DeliveryStatusType status;
    private String errorMessage;
}
