package com.buildledger.service.impl;

import com.buildledger.dto.request.AuditRequestDTO;
import com.buildledger.dto.response.AuditResponseDTO;
import com.buildledger.entity.Audit;
import com.buildledger.entity.User;
import com.buildledger.enums.AuditStatus;
import com.buildledger.exception.InvalidStatusTransitionException;
import com.buildledger.exception.ResourceNotFoundException;
import com.buildledger.repository.AuditRepository;
import com.buildledger.repository.UserRepository;
import com.buildledger.service.AuditService;
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
public class AuditServiceImpl implements AuditService {

    private final AuditRepository auditRepository;
    private final UserRepository userRepository;

    @Override
    public AuditResponseDTO createAudit(AuditRequestDTO request, String officerUsername) {
        log.info("Creating audit by officer: {}", officerUsername);
        User officer = userRepository.findByUsername(officerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", officerUsername));

        Audit audit = Audit.builder()
                .complianceOfficer(officer)
                .scope(request.getScope())
                .findings(request.getFindings())
                .date(request.getDate())
                .status(AuditStatus.SCHEDULED)
                .build();

        return mapAuditToResponse(auditRepository.save(audit));
    }

    @Override
    @Transactional(readOnly = true)
    public AuditResponseDTO getAuditById(Long auditId) {
        return mapAuditToResponse(findAuditById(auditId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditResponseDTO> getAllAudits() {
        return auditRepository.findAll().stream().map(this::mapAuditToResponse).collect(Collectors.toList());
    }

    @Override
    public AuditResponseDTO updateAuditStatus(Long auditId, AuditStatus newStatus, String findings) {
        log.info("Updating audit {} status to {}", auditId, newStatus);

        Audit audit = findAuditById(auditId);
        AuditStatus currentStatus = audit.getStatus();

        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new InvalidStatusTransitionException(
                    currentStatus.name(), newStatus.name()
            );
        }
        audit.setStatus(newStatus);
        // auto-set auditDate when audit actually starts
        if (newStatus == AuditStatus.IN_PROGRESS && audit.getAuditDate() == null) {
            audit.setAuditDate(LocalDate.now());
        }
        if (findings != null) {
            audit.setFindings(findings);
        }
        return mapAuditToResponse(auditRepository.save(audit));
    }

    private Audit findAuditById(Long auditId) {
        return auditRepository.findById(auditId)
                .orElseThrow(() -> new ResourceNotFoundException("Audit", "id", auditId));
    }

    private AuditResponseDTO mapAuditToResponse(Audit a) {
        return AuditResponseDTO.builder()
                .auditId(a.getAuditId())
                .complianceOfficerId(a.getComplianceOfficer().getUserId())
                .officerName(a.getComplianceOfficer().getName())
                .scope(a.getScope())
                .findings(a.getFindings())
                .date(a.getDate())
                .auditDate(a.getAuditDate())
                .status(a.getStatus())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
