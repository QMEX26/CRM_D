package com.crm.service;

import com.crm.dto.request.ProjectCreateRequest;
import com.crm.dto.request.ProjectUpdateRequest;
import com.crm.dto.response.ProjectResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProjectService {
    ProjectResponse createProject(ProjectCreateRequest request, Long currentUserId);
    ProjectResponse updateProject(Long id, ProjectUpdateRequest request, Long currentUserId);
    ProjectResponse toggleProjectStatus(Long id, String status, Long currentUserId);
    ProjectResponse getProjectById(Long id);
    Page<ProjectResponse> searchProjects(String search, String status, Pageable pageable);
    List<ProjectResponse> getActiveProjects();
    List<ProjectResponse> getUserAssignedProjects(Long userId);
    void deleteProject(Long id, Long currentUserId);
}
