package com.example.common.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestDTO2 {
    Long id;
    private String testMessage1;
    private String testMessage2;
    private Integer testNumber;
}