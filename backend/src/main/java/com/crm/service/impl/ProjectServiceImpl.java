package com.crm.service.impl;

import com.crm.dto.request.ProjectCreateRequest;
import com.crm.dto.request.ProjectUpdateRequest;
import com.crm.dto.response.ProjectResponse;
import com.crm.exception.ResourceNotFoundException;
import com.crm.mapper.ProjectMapper;
import com.crm.model.Lead;
import com.crm.model.Project;
import com.crm.repository.LeadRepository;
import com.crm.repository.ProjectRepository;
import com.crm.repository.SalesRepository;
import com.crm.service.AuditService;
import com.crm.service.LeadService;
import com.crm.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final LeadRepository leadRepository;
    private final SalesRepository salesRepository;
    private final ProjectMapper projectMapper;
    private final AuditService auditService;
    @org.springframework.context.annotation.Lazy
    private final LeadService leadService;

    @Override
    @Transactional
    public ProjectResponse createProject(ProjectCreateRequest request, Long currentUserId) {
        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus().toUpperCase() : "ACTIVE")
                .build();

        Project saved = projectRepository.save(project);
        auditService.logAction(currentUserId, "Project", saved.getId(), "CREATE", null, "Created project: " + saved.getName());

        return getProjectStatsResponse(saved);
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(Long id, ProjectUpdateRequest request, Long currentUserId) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        String oldDetails = "Name: " + project.getName() + ", Status: " + project.getStatus();

        project.setName(request.getName());
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus().toUpperCase());
        }

        Project updated = projectRepository.save(project);
        String newDetails = "Name: " + updated.getName() + ", Status: " + updated.getStatus();

        auditService.logAction(currentUserId, "Project", updated.getId(), "UPDATE", oldDetails, newDetails);

        return getProjectStatsResponse(updated);
    }

    @Override
    @Transactional
    public ProjectResponse toggleProjectStatus(Long id, String status, Long currentUserId) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        String oldStatus = project.getStatus();
        String newStatus = (status != null && !status.isBlank())
                ? status.toUpperCase()
                : ("ACTIVE".equalsIgnoreCase(oldStatus) ? "INACTIVE" : "ACTIVE");

        project.setStatus(newStatus);
        Project updated = projectRepository.save(project);

        auditService.logAction(currentUserId, "Project", updated.getId(), "STATUS_CHANGE", oldStatus, updated.getStatus());

        return getProjectStatsResponse(updated);
    }

    @Override
    @Transactional
    public void deleteProject(Long id, Long currentUserId) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        List<Lead> leads = leadRepository.findByProjectId(id);
        for (Lead lead : leads) {
            leadService.deleteLead(lead.getId(), currentUserId);
        }

        projectRepository.delete(project);
        auditService.logAction(currentUserId, "Project", id, "DELETE", project.getName(), "DELETED");
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        return getProjectStatsResponse(project);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectResponse> searchProjects(String search, String status, Pageable pageable) {
        return projectRepository.searchProjects(search, status, pageable)
                .map(this::getProjectStatsResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getActiveProjects() {
        return projectRepository.findByStatus("ACTIVE").stream()
                .map(this::getProjectStatsResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getUserAssignedProjects(Long userId) {
        List<Project> projects = projectRepository.findActiveProjectsAssignedToUser(userId);
        return projects.stream()
                .map(p -> {
                    long userAssignedCount = leadRepository.countAssignedToUserInProject(userId, p.getId());
                    long convertedLeads = salesRepository.countByProjectId(p.getId());
                    return projectMapper.toResponse(p, userAssignedCount, userAssignedCount, convertedLeads);
                })
                .filter(p -> p.getAssignedLeadsCount() > 0)
                .toList();
    }

    private ProjectResponse getProjectStatsResponse(Project project) {
        long totalLeads = leadRepository.countByProjectId(project.getId());
        long convertedLeads = salesRepository.countByProjectId(project.getId());
        return projectMapper.toResponse(project, totalLeads, totalLeads, convertedLeads);
    }
}
