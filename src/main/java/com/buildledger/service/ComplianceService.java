package com.buildledger.service;

import com.buildledger.dto.request.AuditRequest;
import com.buildledger.dto.request.ComplianceRecordRequest;
import com.buildledger.dto.response.AuditResponse;
import com.buildledger.dto.response.ComplianceRecordResponse;
import com.buildledger.enums.AuditStatus;
import com.buildledger.enums.ComplianceStatus;

import java.util.List;

public interface ComplianceService {
    ComplianceRecordResponse createComplianceRecord(ComplianceRecordRequest request);
    ComplianceRecordResponse getComplianceRecordById(Long complianceId);
    ComplianceRecordResponse updateComplianceRecordStatus(Long complianceId, ComplianceStatus newStatus);
    List<ComplianceRecordResponse> getAllComplianceRecords();
    List<ComplianceRecordResponse> getComplianceRecordsByContract(Long contractId);

}
