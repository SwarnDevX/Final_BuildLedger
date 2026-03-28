package com.buildledger.service.impl;

import com.buildledger.dto.request.ServiceRequestDTO;
import com.buildledger.dto.response.ServiceResponseDTO;
import com.buildledger.entity.Contract;
import com.buildledger.enums.ServiceStatus;
import com.buildledger.exception.BadRequestException;
import com.buildledger.exception.ResourceNotFoundException;
import com.buildledger.repository.ContractRepository;
import com.buildledger.repository.ServiceRepository;
import com.buildledger.service.ServiceTrackingService;
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
public class ServiceTrackingServiceImpl implements ServiceTrackingService {

    private final ServiceRepository serviceRepository;
    private final ContractRepository contractRepository;

    @Override
    public ServiceResponseDTO createService(ServiceRequestDTO request) {
        log.info("Creating service record for contract {}", request.getContractId());

        if (!hasRole(com.buildledger.enums.Role.VENDOR)) {
            throw new com.buildledger.exception.BuildLedgerAccessDeniedException("Only vendors or admins can record services.");
        }

        Contract contract = contractRepository.findById(request.getContractId())
                .orElseThrow(() -> new ResourceNotFoundException("Contract", "id", request.getContractId()));

        com.buildledger.entity.Service service = com.buildledger.entity.Service.builder()
                .contract(contract)
                .description(request.getDescription())
                .completionDate(request.getCompletionDate())
                .remarks(request.getRemarks())
                .build();

        return mapToResponse(serviceRepository.save(service));
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceResponseDTO getServiceById(Long serviceId) {
        return mapToResponse(findById(serviceId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceResponseDTO> getAllServices() {
        return serviceRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceResponseDTO> getServicesByContract(Long contractId) {
        return serviceRepository.findByContractContractId(contractId).stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public ServiceResponseDTO updateServiceStatus(Long serviceId, ServiceStatus nextStatus) {
        com.buildledger.entity.Service service = findById(serviceId);
        ServiceStatus currentStatus = service.getStatus();

        if (currentStatus == nextStatus) {
            return mapToResponse(service);
        }


        if (nextStatus == ServiceStatus.IN_PROGRESS || nextStatus == ServiceStatus.COMPLETED) {
            if (!hasRole(com.buildledger.enums.Role.VENDOR) && !hasRole(com.buildledger.enums.Role.ADMIN)) {
                throw new com.buildledger.exception.BuildLedgerAccessDeniedException("Only Vendors or Admins can update service progress.");
            }
        }

        if (nextStatus == ServiceStatus.VERIFIED) {
            if (!hasRole(com.buildledger.enums.Role.PROJECT_MANAGER) && !hasRole(com.buildledger.enums.Role.ADMIN)) {
                throw new com.buildledger.exception.BuildLedgerAccessDeniedException("Only Project Managers or Admins can verify services.");
            }
        }

        // PENDING -> IN_PROGRESS -> COMPLETED -> VERIFIED
        boolean isValid = switch (nextStatus) {
            case IN_PROGRESS -> currentStatus == ServiceStatus.PENDING;
            case COMPLETED -> currentStatus == ServiceStatus.IN_PROGRESS;
            case VERIFIED -> currentStatus == ServiceStatus.COMPLETED;
            case PENDING -> false;
        };

        if (!isValid) {
            throw new BadRequestException("Invalid service status transition from " + currentStatus + " to " + nextStatus + ". The flow must be: Pending -> In_Progress -> Completed -> Verified.");
        }

        service.setStatus(nextStatus);
        return mapToResponse(serviceRepository.save(service));
    }

    private boolean hasRole(com.buildledger.enums.Role role) {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role.name()));
    }


    @Override
    public ServiceResponseDTO updateService(Long serviceId, ServiceRequestDTO request) {
        com.buildledger.entity.Service service = findById(serviceId);

        if(request.getContractId() != null){
            Contract contract = contractRepository.findById(request.getContractId()).orElseThrow(
                    () -> new ResourceNotFoundException("Contract", "ID", request.getContractId())
            );
            service.setContract(contract);
        }

        if (request.getDescription() != null) service.setDescription(request.getDescription());
        if (request.getCompletionDate() != null) service.setCompletionDate(request.getCompletionDate());
        if (request.getRemarks() != null) service.setRemarks(request.getRemarks());
        return mapToResponse(serviceRepository.save(service));
    }

    @Override
    public void deleteService(Long serviceId) {
        serviceRepository.delete(findById(serviceId));
    }

    private com.buildledger.entity.Service findById(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service", "id", id));
    }

    private ServiceResponseDTO mapToResponse(com.buildledger.entity.Service s) {
        return ServiceResponseDTO.builder()
                .serviceId(s.getServiceId())
                .contractId(s.getContract().getContractId())
                .description(s.getDescription())
                .completionDate(s.getCompletionDate())
                .status(s.getStatus())
                .remarks(s.getRemarks())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
