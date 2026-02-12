package com.example.core_service.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class BulkToggleExclusionRequest {
    private List<Integer> contactIds;
    private boolean exclude;
}
