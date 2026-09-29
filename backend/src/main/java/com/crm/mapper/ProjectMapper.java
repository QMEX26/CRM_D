package com.crm.mapper;

import com.crm.dto.response.ProjectResponse;
import com.crm.model.Project;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

    public ProjectResponse toResponse(Project project, long totalLeads, long assignedLeads, long convertedLeads) {
        if (project == null) return null;

        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .status(project.getStatus())
                .createdAt(project.getCreatedAt())
                .totalLeads(totalLeads)
                .assignedLeads(assignedLeads)
                .assignedLeadsCount(totalLeads)
                .convertedLeads(convertedLeads)
                .build();
    }

    public ProjectResponse toResponse(Project project) {
        return toResponse(project, 0, 0, 0);
    }
}
