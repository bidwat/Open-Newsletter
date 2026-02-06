package com.example.core_service.EventPublishers;

import com.example.common.events.TestEvent1;
import com.example.common.events.TestEvent2;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class EventPublishers {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public EventPublishers(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendTestEvent1(TestEvent1 event) {
        kafkaTemplate.send("test-topic", event);
        System.out.println("📤 Sent TestEvent1: " + event.getTestMessage1() + " | " + event.getTestMessage2() + " | ID: " + event.getId());
    }

    public void sendTestEvent2(TestEvent2 event) {
        kafkaTemplate.send("test-topic-4", event);
        System.out.println("📤 Sent TestEvent4: " + event.getTestMessage1() + " | " + event.getTestMessage2() + " | Number: " + event.getTestNumber() + " | ID: " + event.getId());
    }


}
