package com.buildledger.service.impl;

import com.buildledger.dto.request.DeliveryRequestDTO;
import com.buildledger.dto.response.DeliveryResponseDTO;
import com.buildledger.entity.Contract;
import com.buildledger.entity.Delivery;
import com.buildledger.enums.DeliveryStatus;
import com.buildledger.exception.BadRequestException;
import com.buildledger.exception.ResourceNotFoundException;
import com.buildledger.repository.ContractRepository;
import com.buildledger.repository.DeliveryRepository;
import com.buildledger.service.DeliveryService;
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
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final ContractRepository contractRepository;

    @Override
    public DeliveryResponseDTO createDelivery(DeliveryRequestDTO request) {
        log.info("Creating delivery for contract {}", request.getContractId());
        

        if (!hasRole(com.buildledger.enums.Role.VENDOR) && !hasRole(com.buildledger.enums.Role.ADMIN)) {
            throw new com.buildledger.exception.BuildLedgerAccessDeniedException("Only vendors or admins can record deliveries.");
        }

        Contract contract = contractRepository.findById(request.getContractId())
                .orElseThrow(() -> new ResourceNotFoundException("Contract", "id", request.getContractId()));

        Delivery delivery = Delivery.builder()
                .contract(contract)
                .date(request.getDate())
                .item(request.getItem())
                .quantity(request.getQuantity())
                .unit(request.getUnit())
                .remarks(request.getRemarks())
                .build();

        return mapToResponse(deliveryRepository.save(delivery));
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryResponseDTO getDeliveryById(Long deliveryId) {
        return mapToResponse(findById(deliveryId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryResponseDTO> getAllDeliveries() {
        return deliveryRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryResponseDTO> getDeliveriesByContract(Long contractId) {
        return deliveryRepository.findByContractContractId(contractId).stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public DeliveryResponseDTO updateDeliveryStatus(Long deliveryId, DeliveryStatus nextStatus) {
        Delivery delivery = findById(deliveryId);
        DeliveryStatus currentStatus = delivery.getStatus();

        if (currentStatus == nextStatus) {
            return mapToResponse(delivery);
        }


        if (nextStatus == DeliveryStatus.PENDING) {
            throw new BadRequestException("Cannot revert status back to PENDING.");
        }


        if (nextStatus == DeliveryStatus.ACCEPTED) {

            if (!hasRole(com.buildledger.enums.Role.PROJECT_MANAGER) && !hasRole(com.buildledger.enums.Role.ADMIN)) {
                throw new com.buildledger.exception.BuildLedgerAccessDeniedException("Only Project Managers or Admins can accept deliveries.");
            }

            if (currentStatus == DeliveryStatus.PENDING) {
                throw new BadRequestException("Cannot accept a pending delivery.");
            }
            if (currentStatus == DeliveryStatus.DELAYED) {
                throw new BadRequestException("Cannot accept a delayed delivery.");
            }
            if (currentStatus == DeliveryStatus.REJECTED) {
                throw new BadRequestException("Once rejected, cannot accept.");
            }
            if (currentStatus != DeliveryStatus.MARKED_DELIVERED) {
                throw new BadRequestException("Delivery must be marked as delivered by vendor before it can be accepted.");
            }
        }


        if (nextStatus == DeliveryStatus.REJECTED) {

            if (!hasRole(com.buildledger.enums.Role.PROJECT_MANAGER) && !hasRole(com.buildledger.enums.Role.ADMIN)) {
                throw new com.buildledger.exception.BuildLedgerAccessDeniedException("Only Project Managers or Admins can reject deliveries.");
            }

            if (currentStatus == DeliveryStatus.ACCEPTED) {
                throw new BadRequestException("Once accepted, cannot reject.");
            }
        }


        if (nextStatus == DeliveryStatus.MARKED_DELIVERED) {

            if (!hasRole(com.buildledger.enums.Role.VENDOR) && !hasRole(com.buildledger.enums.Role.ADMIN)) {
                throw new com.buildledger.exception.BuildLedgerAccessDeniedException("Only Vendors or Admins can mark deliveries as delivered.");
            }

            if (currentStatus != DeliveryStatus.PENDING && currentStatus != DeliveryStatus.DELAYED) {
                throw new BadRequestException("Only pending or delayed deliveries can be marked as delivered.");
            }
        }


        if (nextStatus == DeliveryStatus.DELAYED) {

            if (!hasRole(com.buildledger.enums.Role.VENDOR) && !hasRole(com.buildledger.enums.Role.ADMIN)) {
                throw new com.buildledger.exception.BuildLedgerAccessDeniedException("Only Vendors or Admins can mark deliveries as delayed.");
            }

            if (currentStatus != DeliveryStatus.PENDING) {
                throw new BadRequestException("Status can only move to DELAYED from PENDING.");
            }

        }

        delivery.setStatus(nextStatus);
        return mapToResponse(deliveryRepository.save(delivery));
    }

    private boolean hasRole(com.buildledger.enums.Role role) {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role.name()));
    }


    @Override
    public DeliveryResponseDTO updateDelivery(Long deliveryId, DeliveryRequestDTO request) {
        Delivery delivery = findById(deliveryId);
        if(request.getContractId() != null){
            Contract contract = contractRepository.findById(request.getContractId()).orElseThrow(
                    () -> new ResourceNotFoundException("Contract", "ID", request.getContractId())
            );
            delivery.setContract(contract);
        }
        if (request.getDate() != null) delivery.setDate(request.getDate());
        if (request.getItem() != null) delivery.setItem(request.getItem());
        if (request.getQuantity() != null) delivery.setQuantity(request.getQuantity());
        if (request.getUnit() != null) delivery.setUnit(request.getUnit());
        if (request.getRemarks() != null) delivery.setRemarks(request.getRemarks());
        return mapToResponse(deliveryRepository.save(delivery));
    }

    @Override
    public void deleteDelivery(Long deliveryId) {
        deliveryRepository.delete(findById(deliveryId));
    }

    private Delivery findById(Long id) {
        return deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery", "id", id));
    }

    private DeliveryResponseDTO mapToResponse(Delivery d) {
        return DeliveryResponseDTO.builder()
                .deliveryId(d.getDeliveryId())
                .contractId(d.getContract().getContractId())
                .date(d.getDate())
                .item(d.getItem())
                .quantity(d.getQuantity())
                .unit(d.getUnit())
                .remarks(d.getRemarks())
                .status(d.getStatus())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }
}
