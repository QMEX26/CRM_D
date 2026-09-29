package com.crm.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectUpdateRequest {
    @NotBlank(message = "Project name is required")
    private String name;

    private String description;
    private String status;
}
