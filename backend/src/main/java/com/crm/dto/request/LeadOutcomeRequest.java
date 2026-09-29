package com.crm.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeadOutcomeRequest {
    private String status; // NEW, CONTACTED, IN_PROGRESS, FOLLOW_UP, CONVERTED, CLOSED
    
    @NotBlank(message = "Business outcome is required")
    private String businessOutcome; // INTERESTED, NOT_INTERESTED, FOLLOW_UP, WRONG_NUMBER, JUNK, CONVERTED
}
