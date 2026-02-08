package com.example.core_service.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ImportContactsResponse {
    private Integer mailingListId;
    private int imported;
    private int skipped;
}
