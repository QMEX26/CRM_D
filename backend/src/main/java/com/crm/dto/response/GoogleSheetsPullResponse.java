package com.crm.dto.response;

import lombok.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoogleSheetsPullResponse {

    @Builder.Default
    private boolean success = true;
    private String syncId;
    @Builder.Default
    private String syncType = "PULL";
    private String timestamp;
    private String triggeredBy;
    private int totalRecords;

    /**
     * Map of table name (e.g. "Roles", "Users", "Leads") to list of column header strings.
     */
    @Builder.Default
    private Map<String, List<String>> headers = new HashMap<>();

    /**
     * Map of table name to 2D array of rows (values) optimized for single-call Google Sheet setValues.
     */
    @Builder.Default
    private Map<String, List<List<Object>>> tables = new HashMap<>();

    /**
     * Record count per table.
     */
    @Builder.Default
    private Map<String, Integer> rowCounts = new HashMap<>();

    private String message;
}
