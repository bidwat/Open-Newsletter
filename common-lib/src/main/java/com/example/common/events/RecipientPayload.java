package com.example.common.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipientPayload {
    private String contactId;
    private String email;
    private String firstName;
    private String lastName;
}
