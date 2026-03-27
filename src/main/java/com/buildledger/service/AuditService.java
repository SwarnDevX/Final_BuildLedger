package com.buildledger.service;

import com.buildledger.dto.request.AuditRequest;
import com.buildledger.dto.response.AuditResponse;
import com.buildledger.enums.AuditStatus;

import java.util.List;

public interface AuditService {

    AuditResponse createAudit(AuditRequest request, String officerUsername);
    AuditResponse getAuditById(Long auditId);
    List<AuditResponse> getAllAudits();
    AuditResponse updateAuditStatus(Long auditId, AuditStatus status, String findings);
}
