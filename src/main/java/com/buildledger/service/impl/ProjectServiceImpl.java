package com.buildledger.service.impl;

import com.buildledger.dto.request.ProjectRequestDTO;
import com.buildledger.dto.response.ProjectResponseDTO;
import com.buildledger.entity.Project;
import com.buildledger.entity.User;
import com.buildledger.enums.ProjectStatus;
import com.buildledger.enums.Role;
import com.buildledger.exception.BadRequestException;
import com.buildledger.exception.InvalidStatusTransitionException;
import com.buildledger.exception.ValidDateException;
import com.buildledger.exception.ResourceNotFoundException;
import com.buildledger.repository.ProjectRepository;
import com.buildledger.repository.UserRepository;
import com.buildledger.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Override
    public ProjectResponseDTO createProject(ProjectRequestDTO request) {
        log.info("Creating project: {}", request.getName());

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new ValidDateException("End date cannot be before start date");
        }

        Project.ProjectBuilder builder = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .location(request.getLocation())
                .budget(request.getBudget())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate());

        if (request.getManagerId() != null) {
            User manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getManagerId()));

            if (manager.getRole() != Role.PROJECT_MANAGER) {
                throw new BadRequestException(
                        "User '" + manager.getName() + "' is not a PROJECT_MANAGER. " +
                                "Only users with PROJECT_MANAGER role can be assigned as project manager."
                );
            }

            builder.manager(manager);
        }
        return mapToResponse(projectRepository.save(builder.build()));
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponseDTO getProjectById(Long projectId) {
        return mapToResponse(findById(projectId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponseDTO> getAllProjects() {
        return projectRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponseDTO> getProjectsByManager(Long managerId) {
        return projectRepository.findByManagerUserId(managerId).stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public ProjectResponseDTO updateProject(Long projectId, ProjectRequestDTO request) {
        Project project = findById(projectId);
        if (request.getName() != null) project.setName(request.getName());
        if (request.getDescription() != null) project.setDescription(request.getDescription());
        if (request.getLocation() != null) project.setLocation(request.getLocation());
        if (request.getBudget() != null) project.setBudget(request.getBudget());
        if (request.getStartDate() != null) project.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) project.setEndDate(request.getEndDate());


        if (request.getManagerId() != null) {
            User manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getManagerId()));

            if (manager.getRole() != Role.PROJECT_MANAGER) {
                throw new BadRequestException(
                        "User '" + manager.getName() + "' is not a PROJECT_MANAGER. " +
                                "Only users with PROJECT_MANAGER role can be assigned as project manager."
                );
            }

            project.setManager(manager);
        }
        return mapToResponse(projectRepository.save(project));
    }

    @Override
    public ProjectResponseDTO updateProjectStatus(Long projectId, ProjectStatus newStatus) {
        log.info("Updating project {} status to {}", projectId, newStatus);

        Project project = findById(projectId);
        ProjectStatus currentStatus = project.getStatus();

        // check if transition is valid
        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new InvalidStatusTransitionException(
                    currentStatus.name(), newStatus.name()
            );
        }

        // auto-set actualEndDate when project is completed
        if (newStatus == ProjectStatus.COMPLETED) {
            project.setActualEndDate(LocalDate.now());
            log.info("Project {} completed. actualEndDate set to {}", projectId, LocalDate.now());
        }

        // if project is put on hold or cancelled, log it
        if (newStatus == ProjectStatus.ON_HOLD) {
            log.info("Project {} put ON_HOLD", projectId);
        }

        if (newStatus == ProjectStatus.CANCELLED) {
            log.info("Project {} CANCELLED", projectId);
        }

        project.setStatus(newStatus);
        return mapToResponse(projectRepository.save(project));
    }

    @Override
    public void deleteProject(Long projectId) {
        Project project = findById(projectId);
        projectRepository.delete(project);
    }

    private Project findById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
    }

    private ProjectResponseDTO mapToResponse(Project p) {
        return ProjectResponseDTO.builder()
                .projectId(p.getProjectId())
                .name(p.getName())
                .description(p.getDescription())
                .location(p.getLocation())
                .budget(p.getBudget())
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())
                .actualEndDate(p.getActualEndDate())
                .status(p.getStatus())
                .managerId(p.getManager() != null ? p.getManager().getUserId() : null)
                .managerName(p.getManager() != null ? p.getManager().getName() : null)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
