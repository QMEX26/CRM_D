package com.crm.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CallClassificationUpdateRequest {

    @NotBlank(message = "Business classification is required")
    private String businessClassification; // VERY_SHORT, SHORT_CALL, INTERESTED, FOLLOW_UP, NOT_INTERESTED, JUNK, SALE, OTHER

    private String notes;
}
