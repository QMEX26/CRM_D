package com.crm.model;

import lombok.Getter;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Getter
public enum WorkShift {
    SHIFT_0900_1800("SHIFT_0900_1800", "09:00 AM – 06:00 PM", LocalTime.of(9, 0), LocalTime.of(18, 0)),
    SHIFT_0930_1830("SHIFT_0930_1830", "09:30 AM – 06:30 PM", LocalTime.of(9, 30), LocalTime.of(18, 30)),
    SHIFT_1000_1900("SHIFT_1000_1900", "10:00 AM – 07:00 PM", LocalTime.of(10, 0), LocalTime.of(19, 0));

    private final String id;
    private final String displayName;
    private final LocalTime startTime;
    private final LocalTime endTime;

    WorkShift(String id, String displayName, LocalTime startTime, LocalTime endTime) {
        this.id = id;
        this.displayName = displayName;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public static WorkShift fromString(String val) {
        if (val == null || val.isBlank()) {
            return SHIFT_1000_1900;
        }
        String clean = val.trim().toUpperCase();
        for (WorkShift shift : values()) {
            if (shift.name().equalsIgnoreCase(clean) || shift.id.equalsIgnoreCase(clean) || shift.displayName.equalsIgnoreCase(val.trim())) {
                return shift;
            }
        }
        if (clean.contains("09:00") || clean.contains("9:00")) {
            return SHIFT_0900_1800;
        }
        if (clean.contains("09:30") || clean.contains("9:30")) {
            return SHIFT_0930_1830;
        }
        if (clean.contains("10:00")) {
            return SHIFT_1000_1900;
        }
        return SHIFT_1000_1900;
    }

    public static List<WorkShift> getAllShifts() {
        return Arrays.asList(SHIFT_0900_1800, SHIFT_0930_1830, SHIFT_1000_1900);
    }
}
