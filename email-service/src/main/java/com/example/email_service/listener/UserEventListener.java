package com.example.email_service.listener;

import com.example.common.events.UserRegisteredEvent;
import com.example.common.kafka.KafkaTopics;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class UserEventListener {

    @KafkaListener(topics = KafkaTopics.USER_REGISTERED, groupId = "email-service-group")
    public void handleUserRegistered(UserRegisteredEvent event) {
        System.out.println("📧 Received Event: Prepared to send welcome email to " + event.getEmail());
    }
}