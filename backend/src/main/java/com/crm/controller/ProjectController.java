package com.crm.controller;

import com.crm.dto.request.ProjectCreateRequest;
import com.crm.dto.request.ProjectUpdateRequest;
import com.crm.dto.response.ApiResponse;
import com.crm.dto.response.PageResponse;
import com.crm.dto.response.ProjectResponse;
import com.crm.security.CurrentUser;
import com.crm.security.UserPrincipal;
import com.crm.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProjectResponse>>> searchProjects(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Page<ProjectResponse> result = projectService.searchProjects(search, status, PageRequest.of(page, size, sort));

        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(result)));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getActiveProjects(@CurrentUser UserPrincipal principal) {
        if (principal != null && !principal.isAdmin()) {
            return ResponseEntity.ok(ApiResponse.ok(projectService.getUserAssignedProjects(principal.getId())));
        }
        return ResponseEntity.ok(ApiResponse.ok(projectService.getActiveProjects()));
    }

    @GetMapping("/my-projects")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getMyProjects(@CurrentUser UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.getUserAssignedProjects(principal.getId())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProjectById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.getProjectById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(@Valid @RequestBody ProjectCreateRequest request,
                                                                     @CurrentUser UserPrincipal principal) {
        ProjectResponse response = projectService.createProject(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Project created successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(@PathVariable Long id,
                                                                     @Valid @RequestBody ProjectUpdateRequest request,
                                                                     @CurrentUser UserPrincipal principal) {
        ProjectResponse response = projectService.updateProject(id, request, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Project updated successfully", response));
    }

    @PatchMapping({ "/{id}/status", "/{id}/toggle-status" })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProjectResponse>> toggleProjectStatus(@PathVariable Long id,
                                                                           @RequestParam(required = false) String status,
                                                                           @RequestBody(required = false) java.util.Map<String, String> body,
                                                                           @CurrentUser UserPrincipal principal) {
        String effectiveStatus = status;
        if (effectiveStatus == null && body != null && body.containsKey("status")) {
            effectiveStatus = body.get("status");
        }
        ProjectResponse response = projectService.toggleProjectStatus(id, effectiveStatus, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Project status updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteProject(@PathVariable Long id,
                                                             @CurrentUser UserPrincipal principal) {
        projectService.deleteProject(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Project deleted successfully", null));
    }
}
