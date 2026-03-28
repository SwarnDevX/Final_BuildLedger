package com.buildledger.service;

import com.buildledger.dto.request.ProjectRequestDTO;
import com.buildledger.dto.response.ProjectResponseDTO;
import com.buildledger.enums.ProjectStatus;
import java.util.List;

public interface ProjectService {
    ProjectResponseDTO createProject(ProjectRequestDTO request);
    ProjectResponseDTO getProjectById(Long projectId);
    List<ProjectResponseDTO> getAllProjects();
    List<ProjectResponseDTO> getProjectsByManager(Long managerId);
    ProjectResponseDTO updateProject(Long projectId, ProjectRequestDTO request);
    void deleteProject(Long projectId);
    ProjectResponseDTO updateProjectStatus(Long projectId, ProjectStatus newStatus); // add this
}