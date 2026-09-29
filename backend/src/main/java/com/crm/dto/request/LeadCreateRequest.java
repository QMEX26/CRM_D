package com.crm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeadCreateRequest {
    @NotNull(message = "Project is required")
    private Long projectId;

    @NotBlank(message = "Customer name is required")
    private String name;

    @NotBlank(message = "Phone number is required")
    private String phone;

    private String email;
    private String address;
    private String city;
    private String state;
    private String source;
    private String status; // NEW, CONTACTED, IN_PROGRESS, FOLLOW_UP, CONVERTED, CLOSED
    private String businessOutcome; // INTERESTED, NOT_INTERESTED, FOLLOW_UP, WRONG_NUMBER, JUNK, CONVERTED
    private String additionalInfo;

    private Long assignedUserId; // Optional initial assignment
}
