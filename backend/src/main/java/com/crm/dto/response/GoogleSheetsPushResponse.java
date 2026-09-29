package com.crm.dto.response;

import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoogleSheetsPushResponse {

    @Builder.Default
    private boolean success = true;
    private String syncId;
    @Builder.Default
    private String syncType = "PUSH";
    private String timestamp;
    private int totalRecords;

    @Builder.Default
    private Map<String, Integer> recordsUpdated = new HashMap<>();

    private String message;
    private String triggeredBy;
}
