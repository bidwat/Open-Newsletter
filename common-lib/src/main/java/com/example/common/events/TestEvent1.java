package com.example.common.events;

import lombok.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TestEvent1 {
    Long id;
    private String testMessage1;
    private String testMessage2;
}