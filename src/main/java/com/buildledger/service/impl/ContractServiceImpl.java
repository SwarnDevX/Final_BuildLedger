package com.buildledger.service.impl;

import com.buildledger.dto.request.ContractRequestDTO;
import com.buildledger.dto.request.ContractTermRequestDTO;
import com.buildledger.dto.response.ContractResponseDTO;
import com.buildledger.dto.response.ContractTermResponseDTO;
import com.buildledger.entity.Contract;
import com.buildledger.entity.ContractTerm;
import com.buildledger.entity.Project;
import com.buildledger.entity.Vendor;
import com.buildledger.enums.ContractStatus;
import com.buildledger.exception.BadRequestException;
import com.buildledger.exception.InvalidStatusTransitionException;
import com.buildledger.exception.ValidDateException;
import com.buildledger.exception.ResourceNotFoundException;
import com.buildledger.repository.ContractRepository;
import com.buildledger.repository.ContractTermRepository;
import com.buildledger.repository.ProjectRepository;
import com.buildledger.repository.VendorRepository;
import com.buildledger.service.ContractService;
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
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;
    private final ContractTermRepository contractTermRepository;
    private final VendorRepository vendorRepository;
    private final ProjectRepository projectRepository;

    @Override
    public ContractResponseDTO createContract(ContractRequestDTO request) {
        log.info("Creating contract for vendor {} and project {}", request.getVendorId(), request.getProjectId());
        //validateDateRange(request.getStartDate(), request.getEndDate());

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new ValidDateException("End date cannot be before start date");
        }

        Vendor vendor = vendorRepository.findById(request.getVendorId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", "id", request.getVendorId()));
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.getProjectId()));

        Contract contract = Contract.builder()
                .vendor(vendor)
                .project(project)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .value(request.getValue())
                .description(request.getDescription())
                .build();

        return mapToResponse(contractRepository.save(contract));
    }

    @Override
    @Transactional(readOnly = true)
    public ContractResponseDTO getContractById(Long contractId) {
        return mapToResponse(findById(contractId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractResponseDTO> getAllContracts() {
        return contractRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractResponseDTO> getContractsByVendor(Long vendorId) {
        return contractRepository.findByVendorVendorId(vendorId).stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractResponseDTO> getContractsByProject(Long projectId) {
        return contractRepository.findByProjectProjectId(projectId).stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractResponseDTO> getContractsByStatus(ContractStatus status) {
        return contractRepository.findByStatus(status).stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public ContractResponseDTO updateContractStatus(Long contractId, ContractStatus newStatus) {
        log.info("Updating contract {} status to {}", contractId, newStatus);

        Contract contract = findById(contractId);
        ContractStatus currentStatus = contract.getStatus();

        if (!(currentStatus.canTransitionTo(newStatus))) {
            throw new InvalidStatusTransitionException(
                    currentStatus.name(), newStatus.name()
            );
        }

        contract.setStatus(newStatus);
        return mapToResponse(contractRepository.save(contract));
    }

    @Override
    public ContractResponseDTO updateContract(Long contractId, ContractRequestDTO request) {
        Contract contract = findById(contractId);

        // block editing if contract is not DRAFT
        if (contract.getStatus() != ContractStatus.DRAFT) {
            throw new BadRequestException(
                    "Contract cannot be edited. Only DRAFT contracts can be modified. " +
                            "Current status: " + contract.getStatus()
            );
        }

        if (request.getStartDate() != null) contract.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) contract.setEndDate(request.getEndDate());
        if (request.getValue() != null) contract.setValue(request.getValue());
        if (request.getDescription() != null) contract.setDescription(request.getDescription());
        if (request.getVendorId() != null) {
            Vendor vendor = vendorRepository.findById(request.getVendorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vendor", "id", request.getVendorId()));
            contract.setVendor(vendor);
        }
        if (request.getProjectId() != null) {
            Project project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.getProjectId()));
            contract.setProject(project);
        }
        return mapToResponse(contractRepository.save(contract));
    }

    @Override
    public void deleteContract(Long contractId) {
        Contract contract = findById(contractId);

        if (contract.getStatus() != ContractStatus.DRAFT) {
            throw new BadRequestException(
                    "Contract cannot be deleted. Only DRAFT contracts can be deleted. " +
                            "Current status: " + contract.getStatus()
            );
        }

        contractRepository.delete(contract);
    }

    @Override
    public ContractTermResponseDTO addContractTerm(Long contractId, ContractTermRequestDTO request) {
        log.info("Adding term to contract {}", contractId);
        Contract contract = findById(contractId);

        if (contract.getStatus() != ContractStatus.DRAFT) {
            throw new BadRequestException(
                    "Cannot add terms to a contract that is not in DRAFT status. " +
                            "Current status: " + contract.getStatus()
            );
        }

        ContractTerm term = ContractTerm.builder()
                .contract(contract)
                .description(request.getDescription())
                .complianceFlag(request.getComplianceFlag() != null ? request.getComplianceFlag() : false)
                .sequenceNumber(request.getSequenceNumber())
                .build();

        return mapTermToResponse(contractTermRepository.save(term));
    }


    @Override
    @Transactional(readOnly = true)
    public List<ContractTermResponseDTO> getContractTerms(Long contractId) {
        findById(contractId); // validate contract exists
        return contractTermRepository.findByContractContractId(contractId).stream()
                .map(this::mapTermToResponse).collect(Collectors.toList());
    }


    @Override
    public ContractTermResponseDTO editContractTerm(Long termId, ContractTermRequestDTO request) {
        ContractTerm term = contractTermRepository.findById(termId)
                .orElseThrow(() -> new ResourceNotFoundException("ContractTerm", "id", termId));

        // check parent contract status
        if (term.getContract().getStatus() != ContractStatus.DRAFT) {
            throw new BadRequestException(
                    "Cannot edit terms of a contract that is not in DRAFT status. " +
                            "Current status: " + term.getContract().getStatus()
            );
        }

        if (request.getDescription() != null) term.setDescription(request.getDescription());
        if (request.getComplianceFlag() != null) term.setComplianceFlag(request.getComplianceFlag());
        if (request.getSequenceNumber() != null) term.setSequenceNumber(request.getSequenceNumber());
        return mapTermToResponse(contractTermRepository.save(term));
    }

    @Override
    public void deleteContractTerm(Long termId) {
        ContractTerm term = contractTermRepository.findById(termId)
                .orElseThrow(() -> new ResourceNotFoundException("ContractTerm", "id", termId));

        // check parent contract status
        if (term.getContract().getStatus() != ContractStatus.DRAFT) {
            throw new BadRequestException(
                    "Cannot delete terms of a contract that is not in DRAFT status. " +
                            "Current status: " + term.getContract().getStatus()
            );
        }

        contractTermRepository.delete(term);
    }

    private Contract findById(Long contractId) {
        return contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", "id", contractId));
    }

    private ContractResponseDTO mapToResponse(Contract c) {
        return ContractResponseDTO.builder()
                .contractId(c.getContractId())
                .vendorId(c.getVendor().getVendorId())
                .vendorName(c.getVendor().getName())
                .projectId(c.getProject().getProjectId())
                .projectName(c.getProject().getName())
                .startDate(c.getStartDate())
                .endDate(c.getEndDate())
                .value(c.getValue())
                .status(c.getStatus())
                .description(c.getDescription())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    private ContractTermResponseDTO mapTermToResponse(ContractTerm t) {
        return ContractTermResponseDTO.builder()
                .termId(t.getTermId())
                .contractId(t.getContract().getContractId())
                .description(t.getDescription())
                .complianceFlag(t.getComplianceFlag())
                .sequenceNumber(t.getSequenceNumber())
                .createdAt(t.getCreatedAt())
                .build();
    }
}
