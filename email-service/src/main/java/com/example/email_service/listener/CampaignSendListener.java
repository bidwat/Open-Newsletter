package com.example.email_service.listener;

import com.example.common.events.DeliveryStatusEvent;
import com.example.common.events.DeliveryStatusType;
import com.example.common.events.SendCampaignEvent;
import com.example.common.kafka.KafkaTopics;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class CampaignSendListener {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public CampaignSendListener(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = KafkaTopics.SEND_CAMPAIGN, groupId = "email-service-group")
    public void handleSendCampaign(SendCampaignEvent event) {
        if (event == null || event.getRecipients() == null) {
            return;
        }

        event.getRecipients().forEach(recipient -> {
            System.out.println("📧 Sending campaign " + event.getCampaignId() + " to " + recipient.getEmail());
            DeliveryStatusEvent statusEvent = new DeliveryStatusEvent(
                    event.getCampaignId(),
                    event.getUserId(),
                    recipient.getContactId(),
                    recipient.getEmail(),
                    DeliveryStatusType.SENT,
                    null
            );
            kafkaTemplate.send(KafkaTopics.DELIVERY_STATUS, statusEvent);
        });
    }
}
