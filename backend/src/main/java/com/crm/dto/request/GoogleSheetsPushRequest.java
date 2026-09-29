package com.crm.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleSheetsPushRequest {

    private String syncId;
    private String secret;
    private String triggeredBy;

    /**
     * Map of sheet/table name (e.g. "roles", "users", "leads", "calls", etc.)
     * to a list of row maps (columnName -> cellValue).
     */
    @Builder.Default
    private Map<String, List<Map<String, Object>>> tables = new HashMap<>();
}
