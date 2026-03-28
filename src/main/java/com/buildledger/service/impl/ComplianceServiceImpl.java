package com.buildledger.service.impl;

import com.buildledger.dto.request.ComplianceRecordRequestDTO;
import com.buildledger.dto.response.ComplianceRecordResponseDTO;
import com.buildledger.entity.ComplianceRecord;
import com.buildledger.entity.Contract;
import com.buildledger.enums.ComplianceStatus;
import com.buildledger.exception.InvalidStatusTransitionException;
import com.buildledger.exception.ResourceNotFoundException;
import com.buildledger.repository.AuditRepository;
import com.buildledger.repository.ComplianceRecordRepository;
import com.buildledger.repository.ContractRepository;
import com.buildledger.repository.UserRepository;
import com.buildledger.service.ComplianceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ComplianceServiceImpl implements ComplianceService {

    private final ComplianceRecordRepository complianceRecordRepository;
    private final AuditRepository auditRepository;
    private final ContractRepository contractRepository;
    private final UserRepository userRepository;

    @Override
    public ComplianceRecordResponseDTO createComplianceRecord(ComplianceRecordRequestDTO request) {
        log.info("Creating compliance record for contract {}", request.getContractId());
        Contract contract = contractRepository.findById(request.getContractId())
                .orElseThrow(() -> new ResourceNotFoundException("Contract", "id", request.getContractId()));

        ComplianceRecord record = ComplianceRecord.builder()
                .contract(contract)
                .type(request.getType())
                .result(request.getResult())
                .date(request.getDate())
                .notes(request.getNotes())
                .status(ComplianceStatus.PENDING)   // always starts as pending
                .build();

        return mapRecordToResponse(complianceRecordRepository.save(record));
    }

    @Override
    @Transactional(readOnly = true)
    public ComplianceRecordResponseDTO getComplianceRecordById(Long complianceId) {
        ComplianceRecord record = complianceRecordRepository.findById(complianceId)
                .orElseThrow(() -> new ResourceNotFoundException("ComplianceRecord", "id", complianceId));
        return mapRecordToResponse(record);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComplianceRecordResponseDTO> getAllComplianceRecords() {
        return complianceRecordRepository.findAll().stream()
                .map(this::mapRecordToResponse).collect(Collectors.toList());
    }

    @Override
    public ComplianceRecordResponseDTO updateComplianceRecordStatus(Long complianceId, ComplianceStatus newStatus) {
        log.info("Updating compliance record {} status to {}", complianceId, newStatus);

        ComplianceRecord record = complianceRecordRepository.findById(complianceId)
                .orElseThrow(() -> new ResourceNotFoundException("ComplianceRecord", "id", complianceId));

        ComplianceStatus currentStatus = record.getStatus();

        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new InvalidStatusTransitionException(
                    currentStatus.name(), newStatus.name()
            );
        }

        record.setStatus(newStatus);
        return mapRecordToResponse(complianceRecordRepository.save(record));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComplianceRecordResponseDTO> getComplianceRecordsByContract(Long contractId) {
        return complianceRecordRepository.findByContractContractId(contractId).stream()
                .map(this::mapRecordToResponse).collect(Collectors.toList());
    }

    private ComplianceRecordResponseDTO mapRecordToResponse(ComplianceRecord r) {
        return ComplianceRecordResponseDTO.builder()
                .complianceId(r.getComplianceId())
                .contractId(r.getContract().getContractId())
                .type(r.getType())
                .result(r.getResult())
                .date(r.getDate())
                .notes(r.getNotes())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .build();
    }

}
