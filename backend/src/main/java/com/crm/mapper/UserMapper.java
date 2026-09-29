package com.crm.mapper;

import com.crm.dto.response.UserResponse;
import com.crm.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user, long activeLeadsCount, long totalCallsCount) {
        if (user == null) return null;

        com.crm.model.WorkShift shift = user.getShift() != null ? user.getShift() : com.crm.model.WorkShift.SHIFT_1000_1900;
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole() != null ? user.getRole().getName() : null)
                .status(user.getStatus())
                .shift(shift.getId())
                .shiftDisplayName(shift.getDisplayName())
                .shiftStartTime(shift.getStartTime())
                .shiftEndTime(shift.getEndTime())
                .createdAt(user.getCreatedAt())
                .activeLeadsCount(activeLeadsCount)
                .totalCallsCount(totalCallsCount)
                .build();
    }

    public UserResponse toResponse(User user) {
        return toResponse(user, 0, 0);
    }
}
