package com.buildledger.service;

import com.buildledger.dto.request.AuditRequestDTO;
import com.buildledger.dto.response.AuditResponseDTO;
import com.buildledger.enums.AuditStatus;

import java.util.List;

public interface AuditService {

    AuditResponseDTO createAudit(AuditRequestDTO request, String officerUsername);
    AuditResponseDTO getAuditById(Long auditId);
    List<AuditResponseDTO> getAllAudits();
    AuditResponseDTO updateAuditStatus(Long auditId, AuditStatus status, String findings);
}
