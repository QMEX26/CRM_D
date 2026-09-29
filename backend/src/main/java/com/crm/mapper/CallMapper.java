package com.crm.mapper;

import com.crm.dto.response.CallResponse;
import com.crm.model.Call;
import org.springframework.stereotype.Component;

@Component
public class CallMapper {

    public CallResponse toResponse(Call call) {
        if (call == null) return null;

        return CallResponse.builder()
                .id(call.getId())
                .telephonyCallId(call.getTelephonyCallId())
                .leadId(call.getLead() != null ? call.getLead().getId() : null)
                .leadName(call.getLead() != null ? call.getLead().getName() : null)
                .leadPhone(call.getLead() != null ? call.getLead().getPhone() : call.getPhoneNumber())
                .phoneNumber(call.getPhoneNumber() != null ? call.getPhoneNumber() : (call.getLead() != null ? call.getLead().getPhone() : null))
                .isConnected(Boolean.TRUE.equals(call.getIsConnected()))
                .userId(call.getUser() != null ? call.getUser().getId() : null)
                .userName(call.getUser() != null ? call.getUser().getName() : null)
                .callDirection(call.getCallDirection())
                .callLifecycleStatus(call.getCallLifecycleStatus())
                .startedAt(call.getStartedAt())
                .connectedAt(call.getConnectedAt())
                .endedAt(call.getEndedAt())
                .durationSeconds(call.getDurationSeconds())
                .callStatus(call.getCallStatus())
                .businessOutcome(call.getBusinessOutcome())
                .automaticClassification(call.getAutomaticClassification())
                .finalClassification(call.getFinalClassification())
                .classificationChangedManually(call.getClassificationChangedManually())
                .classificationChangedByName(call.getClassificationChangedBy() != null ? call.getClassificationChangedBy().getName() : null)
                .classificationChangedAt(call.getClassificationChangedAt())
                .followUpRequired(call.getFollowUpRequired())
                .followUpDate(call.getFollowUpDate())
                .followUpId(call.getFollowUpId())
                .notes(call.getNotes())
                .createdAt(call.getCreatedAt())
                .build();
    }
}
