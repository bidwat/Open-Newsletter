package com.example.core_service.service;

import com.example.common.events.SendCampaignEvent;
import com.example.common.events.UserRegisteredEvent;
import com.example.common.kafka.KafkaTopics;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendUserRegisteredEvent(UserRegisteredEvent event) {
        kafkaTemplate.send(KafkaTopics.USER_REGISTERED, event);
        System.out.println("Message sent to Kafka topic: " + event.getEmail());
    }

    public void sendCampaignEvent(SendCampaignEvent event) {
        kafkaTemplate.send(KafkaTopics.SEND_CAMPAIGN, event);
        System.out.println("Campaign send event published: " + event.getCampaignId());
    }
}