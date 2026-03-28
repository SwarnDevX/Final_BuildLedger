package com.buildledger.service;

import com.buildledger.dto.request.ComplianceRecordRequestDTO;
import com.buildledger.dto.response.ComplianceRecordResponseDTO;
import com.buildledger.enums.ComplianceStatus;

import java.util.List;

public interface ComplianceService {
    ComplianceRecordResponseDTO createComplianceRecord(ComplianceRecordRequestDTO request);
    ComplianceRecordResponseDTO getComplianceRecordById(Long complianceId);
    ComplianceRecordResponseDTO updateComplianceRecordStatus(Long complianceId, ComplianceStatus newStatus);
    List<ComplianceRecordResponseDTO> getAllComplianceRecords();
    List<ComplianceRecordResponseDTO> getComplianceRecordsByContract(Long contractId);

}
