package com.example.common.events;

import lombok.*;

@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestEvent2 {
    Long id;
    private String testMessage1;
    private String testMessage2;
    private Integer testNumber;
}