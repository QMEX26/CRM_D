package com.crm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadSummaryResponse {
    private Long id;
    private Long projectId;
    private String projectName;
    private String name;
    private String phone;
    private String email;
    private String city;
    private String source;
    private String status;
    private String businessOutcome;
    private Long currentOwnerId;
    private String currentOwnerName;
    private LocalDateTime createdAt;

    public UserResponse getCurrentOwner() {
        if (currentOwnerId == null && currentOwnerName == null) {
            return null;
        }
        return UserResponse.builder()
                .id(currentOwnerId)
                .name(currentOwnerName)
                .build();
    }
}
