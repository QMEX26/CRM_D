package com.crm.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShiftReviewRequest {

    @NotBlank(message = "Status is required (APPROVED or REJECTED)")
    private String status;

    private String adminNotes;
}
