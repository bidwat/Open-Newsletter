package com.example.email_service.listener;

import com.example.common.events.TestEvent1;
import com.example.common.events.TestEvent2;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class EventListeners {

    @KafkaListener(topics = "test-topic", groupId = "email-service-group")
    public void listen(TestEvent1 event) {
        System.out.println("📧 Received TestEvent1: " + event.getTestMessage1() + " | " + event.getTestMessage2() + " | ID: " + event.getId() + "on topic: test-topic");
    }

    @KafkaListener(topics="test-topic-4", groupId = "email-service-group")
    public void listenTestEvent2(TestEvent2 event) {
        System.out.println("📧 Received TestEvent2: " + event.getTestMessage1() + " | " + event.getTestMessage2() + " | Number: " + event.getTestNumber() + " | ID: " + event.getId() + "on topic: test-topic-4");
    }
}
