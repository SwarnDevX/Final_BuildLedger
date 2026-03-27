package com.buildledger.service.impl;

import com.buildledger.dto.request.InvoiceRequest;
import com.buildledger.dto.response.InvoiceResponse;
import com.buildledger.entity.Contract;
import com.buildledger.entity.Invoice;
import com.buildledger.enums.InvoiceStatus;
import com.buildledger.exception.BadRequestException;
import com.buildledger.exception.ResourceNotFoundException;
import com.buildledger.repository.ContractRepository;
import com.buildledger.repository.InvoiceRepository;
import com.buildledger.service.InvoiceService;
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
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ContractRepository contractRepository;

    @Override
    public InvoiceResponse submitInvoice(InvoiceRequest request) {
        log.info("Submitting invoice for contract {}", request.getContractId());
        Contract contract = contractRepository.findById(request.getContractId())
                .orElseThrow(() -> new ResourceNotFoundException("Contract", "id", request.getContractId()));

        Invoice invoice = Invoice.builder()
                .contract(contract)
                .amount(request.getAmount())
                .date(request.getDate())
                .dueDate(request.getDueDate())
                .description(request.getDescription())
                .status(InvoiceStatus.UNDER_REVIEW)
                .build();

        return mapToResponse(invoiceRepository.save(invoice));
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceById(Long invoiceId) {
        return mapToResponse(findById(invoiceId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getAllInvoices() {
        return invoiceRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getInvoicesByContract(Long contractId) {
        return invoiceRepository.findByContractContractId(contractId).stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getInvoicesByStatus(InvoiceStatus status) {
        return invoiceRepository.findByStatus(status).stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public InvoiceResponse approveInvoice(Long invoiceId) {
        log.info("Approving invoice {}", invoiceId);
        Invoice invoice = findById(invoiceId);
        if (invoice.getStatus() != InvoiceStatus.UNDER_REVIEW && invoice.getStatus() != InvoiceStatus.UNDER_REVIEW) {
            throw new BadRequestException("Invoice can only be approved when in SUBMITTED or UNDER_REVIEW status. Current: " + invoice.getStatus());
        }
        invoice.setStatus(InvoiceStatus.APPROVED);
        return mapToResponse(invoiceRepository.save(invoice));
    }

    @Override
    public InvoiceResponse rejectInvoice(Long invoiceId, String reason) {
        log.info("Rejecting invoice {}", invoiceId);
        Invoice invoice = findById(invoiceId);
        if (invoice.getStatus() != InvoiceStatus.UNDER_REVIEW && invoice.getStatus() != InvoiceStatus.UNDER_REVIEW) {
            throw new BadRequestException("Invoice can only be rejected when in SUBMITTED or UNDER_REVIEW status. Current: " + invoice.getStatus());
        }
        invoice.setStatus(InvoiceStatus.REJECTED);
        invoice.setRejectionReason(reason);
        return mapToResponse(invoiceRepository.save(invoice));
    }

    @Override
    public void deleteInvoice(Long invoiceId) {
        Invoice invoice = findById(invoiceId);
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new BadRequestException("Cannot delete a paid invoice.");
        }
        invoiceRepository.delete(invoice);
    }

    private Invoice findById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));
    }

    private InvoiceResponse mapToResponse(Invoice i) {
        return InvoiceResponse.builder()
                .invoiceId(i.getInvoiceId())
                .contractId(i.getContract().getContractId())
                .contractVendorName(i.getContract().getVendor().getName())
                .amount(i.getAmount())
                .date(i.getDate())
                .dueDate(i.getDueDate())
                .description(i.getDescription())
                .status(i.getStatus())
                .rejectionReason(i.getRejectionReason())
                .createdAt(i.getCreatedAt())
                .updatedAt(i.getUpdatedAt())
                .build();
    }
}
