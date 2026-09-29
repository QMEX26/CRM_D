package com.crm.mapper;

import com.crm.dto.response.*;
import com.crm.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LeadMapper {

    private final ProjectMapper projectMapper;
    private final UserMapper userMapper;
    private final CallMapper callMapper;
    private final FollowUpMapper followUpMapper;
    private final NoteMapper noteMapper;
    private final SaleMapper saleMapper;

    public LeadSummaryResponse toSummaryResponse(Lead lead, LeadAssignment activeAssignment) {
        if (lead == null) return null;

        Long currentOwnerId = null;
        String currentOwnerName = null;
        if (activeAssignment != null && activeAssignment.getUser() != null) {
            currentOwnerId = activeAssignment.getUser().getId();
            currentOwnerName = activeAssignment.getUser().getName();
        }

        return LeadSummaryResponse.builder()
                .id(lead.getId())
                .projectId(lead.getProject() != null ? lead.getProject().getId() : null)
                .projectName(lead.getProject() != null ? lead.getProject().getName() : null)
                .name(lead.getName())
                .phone(lead.getPhone())
                .email(lead.getEmail())
                .city(lead.getCity())
                .source(lead.getSource())
                .status(lead.getStatus())
                .businessOutcome(lead.getBusinessOutcome())
                .currentOwnerId(currentOwnerId)
                .currentOwnerName(currentOwnerName)
                .createdAt(lead.getCreatedAt())
                .build();
    }

    public AssignmentHistoryResponse toAssignmentHistoryResponse(LeadAssignment assignment) {
        if (assignment == null) return null;

        return AssignmentHistoryResponse.builder()
                .id(assignment.getId())
                .leadId(assignment.getLead() != null ? assignment.getLead().getId() : null)
                .userId(assignment.getUser() != null ? assignment.getUser().getId() : null)
                .userName(assignment.getUser() != null ? assignment.getUser().getName() : null)
                .userEmail(assignment.getUser() != null ? assignment.getUser().getEmail() : null)
                .assignedById(assignment.getAssignedBy() != null ? assignment.getAssignedBy().getId() : null)
                .assignedByName(assignment.getAssignedBy() != null ? assignment.getAssignedBy().getName() : null)
                .assignedAt(assignment.getAssignedAt())
                .unassignedAt(assignment.getUnassignedAt())
                .isActive(assignment.getIsActive())
                .build();
    }

    public LeadDetailResponse toDetailResponse(Lead lead,
                                               LeadAssignment activeAssignment,
                                               List<User> previousOwners,
                                               List<LeadAssignment> assignmentHistory,
                                               CallSummaryStats callSummary,
                                               List<Call> calls,
                                               List<FollowUp> followUps,
                                               List<Note> notes,
                                               Sale sale) {
        if (lead == null) return null;

        UserResponse currentOwnerDto = (activeAssignment != null && activeAssignment.getUser() != null)
                ? userMapper.toResponse(activeAssignment.getUser())
                : null;

        List<UserResponse> previousOwnerDtos = previousOwners.stream()
                .map(userMapper::toResponse)
                .toList();

        List<AssignmentHistoryResponse> assignmentHistoryDtos = assignmentHistory.stream()
                .map(this::toAssignmentHistoryResponse)
                .toList();

        List<CallResponse> callDtos = calls.stream()
                .map(callMapper::toResponse)
                .toList();

        List<FollowUpResponse> followUpDtos = followUps.stream()
                .map(followUpMapper::toResponse)
                .toList();

        List<NoteResponse> noteDtos = notes.stream()
                .map(noteMapper::toResponse)
                .toList();

        SaleResponse saleDto = sale != null ? saleMapper.toResponse(sale) : null;

        return LeadDetailResponse.builder()
                .id(lead.getId())
                .project(lead.getProject() != null ? projectMapper.toResponse(lead.getProject()) : null)
                .name(lead.getName())
                .phone(lead.getPhone())
                .email(lead.getEmail())
                .address(lead.getAddress())
                .city(lead.getCity())
                .state(lead.getState())
                .source(lead.getSource())
                .status(lead.getStatus())
                .businessOutcome(lead.getBusinessOutcome())
                .additionalInfo(lead.getAdditionalInfo())
                .createdAt(lead.getCreatedAt())
                .updatedAt(lead.getUpdatedAt())
                .currentOwner(currentOwnerDto)
                .previousOwners(previousOwnerDtos)
                .assignmentHistory(assignmentHistoryDtos)
                .callSummary(callSummary)
                .callHistory(callDtos)
                .followUps(followUpDtos)
                .notes(noteDtos)
                .sale(saleDto)
                .build();
    }
}
