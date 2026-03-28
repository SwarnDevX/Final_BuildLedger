package com.buildledger.service.impl;

import com.buildledger.dto.request.PaymentRequestDTO;
import com.buildledger.dto.response.PaymentResponseDTO;
import com.buildledger.entity.Invoice;
import com.buildledger.entity.Payment;
import com.buildledger.enums.InvoiceStatus;
import com.buildledger.enums.PaymentStatus;
import com.buildledger.exception.BadRequestException;
import com.buildledger.exception.InvalidStatusTransitionException;
import com.buildledger.exception.ResourceNotFoundException;
import com.buildledger.repository.InvoiceRepository;
import com.buildledger.repository.PaymentRepository;
import com.buildledger.service.PaymentService;
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
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;

    @Override
    public PaymentResponseDTO processPayment(PaymentRequestDTO request) {
        log.info("Processing payment for invoice {}", request.getInvoiceId());

        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", request.getInvoiceId()));

        if (invoice.getStatus() != InvoiceStatus.APPROVED) {
            throw new BadRequestException(
                    "Payment can only be processed for APPROVED invoices. Current status: "
                            + invoice.getStatus()
            );
        }

        Payment payment = Payment.builder()
                .invoice(invoice)
                .amount(request.getAmount())
                .date(request.getDate())
                .method(request.getMethod())
                .status(PaymentStatus.PENDING)
                .transactionReference(request.getTransactionReference())
                .remarks(request.getRemarks())
                .build();

        return mapToResponse(paymentRepository.save(payment));
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDTO getPaymentById(Long paymentId) {
        return mapToResponse(findById(paymentId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> getPaymentsByInvoice(Long invoiceId) {
        return paymentRepository.findByInvoiceInvoiceId(invoiceId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentResponseDTO updatePaymentStatus(Long paymentId, PaymentStatus newStatus) {
        log.info("Updating payment {} status to {}", paymentId, newStatus);

        Payment payment = findById(paymentId);
        PaymentStatus currentStatus = payment.getStatus();

        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new InvalidStatusTransitionException(
                    currentStatus.name(), newStatus.name()
            );
        }

        payment.setStatus(newStatus);

        if (newStatus == PaymentStatus.COMPLETED) {
            Invoice invoice = payment.getInvoice();
            invoice.setStatus(InvoiceStatus.PAID);
            invoiceRepository.save(invoice);
            log.info("Invoice {} marked as PAID after payment {} completed",
                    invoice.getInvoiceId(), paymentId);
        }

        if (newStatus == PaymentStatus.REVERSED) {
            Invoice invoice = payment.getInvoice();
            invoice.setStatus(InvoiceStatus.APPROVED);
            invoiceRepository.save(invoice);
            log.info("Invoice {} reopened to APPROVED after payment {} reversed",
                    invoice.getInvoiceId(), paymentId);
        }

        return mapToResponse(paymentRepository.save(payment));
    }

    private Payment findById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));
    }

    private PaymentResponseDTO mapToResponse(Payment p) {
        return PaymentResponseDTO.builder()
                .paymentId(p.getPaymentId())
                .invoiceId(p.getInvoice().getInvoiceId())
                .amount(p.getAmount())
                .date(p.getDate())
                .method(p.getMethod())
                .status(p.getStatus())
                .transactionReference(p.getTransactionReference())
                .remarks(p.getRemarks())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}