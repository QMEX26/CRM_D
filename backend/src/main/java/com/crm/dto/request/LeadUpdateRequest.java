package com.crm.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeadUpdateRequest {
    @NotBlank(message = "Customer name is required")
    private String name;

    @NotBlank(message = "Phone number is required")
    private String phone;

    private String email;
    private String address;
    private String city;
    private String state;
    private String source;
    private String status;
    private String businessOutcome;
    private String additionalInfo;
}
