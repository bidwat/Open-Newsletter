package com.example.core_service;

import com.example.common.dtos.TestDTO1;
import com.example.common.dtos.TestDTO2;
import com.example.common.events.TestEvent1;
import com.example.common.events.TestEvent2;
import com.example.core_service.EventPublishers.EventPublishers;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.http.HttpResponse;

@RestController
@RequestMapping("/test")
public class TestController {

    EventPublishers eventPublishers;

    TestController(EventPublishers eventPublishers) {
        this.eventPublishers = eventPublishers;
    }

    @GetMapping("")
    public String test() {
        return "Test successful";
    }

    @GetMapping("/me")
    public Object me(@AuthenticationPrincipal Jwt jwt) {
        return java.util.Map.of(
                "sub", jwt.getSubject(),
                "issuer", jwt.getIssuer().toString(),
                "aud", jwt.getAudience()
        );
    }

    @PostMapping("/1")
    public TestDTO1 testEvent1(@RequestBody TestDTO1 testDTO1) {
        TestEvent1 event = new TestEvent1(testDTO1.getId(), testDTO1.getTestMessage1(), testDTO1.getTestMessage2());
        eventPublishers.sendTestEvent1(event);
        System.out.println("Received TestDTO1: " + testDTO1.getTestMessage1() + " | " + testDTO1.getTestMessage2());
        return testDTO1;
    }

    @PostMapping("/2")
    public TestDTO2 testEvent2(@RequestBody TestDTO2 testDTO2) {
        TestEvent2 event = new TestEvent2(testDTO2.getId(), testDTO2.getTestMessage1(), testDTO2.getTestMessage2(), testDTO2.getTestNumber());
        eventPublishers.sendTestEvent2(event);
        System.out.println("Received TestDTO2: " + testDTO2.getTestMessage1() + " | " + testDTO2.getTestMessage2() + " | Number: " + testDTO2.getTestNumber());
        return testDTO2;
    }

}
