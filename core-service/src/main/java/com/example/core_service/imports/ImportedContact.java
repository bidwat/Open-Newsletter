package com.example.core_service.imports;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ImportedContact {
    private String email;
    private String firstName;
    private String lastName;
}
