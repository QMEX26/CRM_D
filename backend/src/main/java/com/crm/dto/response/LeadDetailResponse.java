package com.crm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadDetailResponse {
    private Long id;
    private ProjectResponse project;
    private String name;
    private String phone;
    private String email;
    private String address;
    private String city;
    private String state;
    private String source;
    private String status;
    private String businessOutcome;
    private String additionalInfo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Ownership
    private UserResponse currentOwner;
    private List<UserResponse> previousOwners;
    private List<AssignmentHistoryResponse> assignmentHistory;

    // Interactions
    private CallSummaryStats callSummary;
    private List<CallResponse> callHistory;
    private List<FollowUpResponse> followUps;
    private List<NoteResponse> notes;
    private SaleResponse sale;
}
