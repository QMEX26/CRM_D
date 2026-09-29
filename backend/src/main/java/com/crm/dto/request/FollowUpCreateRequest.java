package com.crm.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowUpCreateRequest {
    @NotNull(message = "Lead ID is required")
    private Long leadId;

    @JsonAlias({"followUpDate", "scheduledDate", "date"})
    private LocalDateTime scheduledTime;

    private String notes;

    public LocalDateTime getScheduledTime() {
        return scheduledTime != null ? scheduledTime : LocalDateTime.now().plusDays(1);
    }
}
