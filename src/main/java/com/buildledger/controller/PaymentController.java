package com.buildledger.controller;

import com.buildledger.dto.request.PaymentRequestDTO;
import com.buildledger.dto.response.ApiResponseDTO;
import com.buildledger.dto.response.PaymentResponseDTO;
import com.buildledger.enums.PaymentStatus;
import com.buildledger.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Processing", description = "Payment tracking | FINANCE_OFFICER: process & update payments")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasRole('FINANCE_OFFICER')")
    @Operation(summary = "Process payment [FINANCE_OFFICER only]",
               description = "✅ Only FINANCE_OFFICER can process payments for approved invoices.\n❌ VENDOR, PROJECT_MANAGER, COMPLIANCE_OFFICER, ADMIN cannot process payments.")
    public ResponseEntity<ApiResponseDTO<PaymentResponseDTO>> processPayment(@Valid @RequestBody PaymentRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success("Payment processed successfully", paymentService.processPayment(request)));
    }

    @GetMapping
    @Operation(summary = "Get all payments [PUBLIC]")
    public ResponseEntity<ApiResponseDTO<List<PaymentResponseDTO>>> getAllPayments() {
        return ResponseEntity.ok(ApiResponseDTO.success("Payments retrieved", paymentService.getAllPayments()));
    }

    @GetMapping("/{paymentId}")
    @Operation(summary = "Get payment by ID [PUBLIC]")
    public ResponseEntity<ApiResponseDTO<PaymentResponseDTO>> getPaymentById(@PathVariable Long paymentId) {
        return ResponseEntity.ok(ApiResponseDTO.success("Payment retrieved", paymentService.getPaymentById(paymentId)));
    }

    @GetMapping("/invoice/{invoiceId}")
    @Operation(summary = "Get payments by invoice [PUBLIC]")
    public ResponseEntity<ApiResponseDTO<List<PaymentResponseDTO>>> getPaymentsByInvoice(@PathVariable Long invoiceId) {
        return ResponseEntity.ok(ApiResponseDTO.success("Payments retrieved", paymentService.getPaymentsByInvoice(invoiceId)));
    }

    @PatchMapping("/{paymentId}/status")
    @PreAuthorize("hasRole('FINANCE_OFFICER')")
    @Operation(summary = "Update payment status [FINANCE_OFFICER only]",
               description = "✅ Only FINANCE_OFFICER can update payment status.\n❌ No other role can modify payment records.")
    public ResponseEntity<ApiResponseDTO<PaymentResponseDTO>> updatePaymentStatus(
            @PathVariable Long paymentId,
            @RequestParam PaymentStatus status) {
        return ResponseEntity.ok(ApiResponseDTO.success("Payment status updated",
                paymentService.updatePaymentStatus(paymentId, status)));
    }
}
