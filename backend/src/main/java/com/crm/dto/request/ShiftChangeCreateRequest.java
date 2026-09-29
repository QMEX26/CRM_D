package com.crm.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShiftChangeCreateRequest {

    @NotBlank(message = "Requested shift is required")
    private String requestedShift;

    private String reason;
}
