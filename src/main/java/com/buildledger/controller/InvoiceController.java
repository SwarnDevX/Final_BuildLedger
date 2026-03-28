package com.buildledger.controller;

import com.buildledger.dto.request.InvoiceRequestDTO;
import com.buildledger.dto.response.ApiResponseDTO;
import com.buildledger.dto.response.InvoiceResponseDTO;
import com.buildledger.enums.InvoiceStatus;
import com.buildledger.service.InvoiceService;
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
@RequestMapping("/invoices")
@RequiredArgsConstructor
@Tag(name = "Invoice Management", description = "Invoice workflow | VENDOR: submit | FINANCE_OFFICER: approve/reject")
@SecurityRequirement(name = "bearerAuth")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping
    @PreAuthorize("hasRole('VENDOR')")
    @Operation(summary = "Submit invoice [VENDOR only]",
               description = "✅ Only VENDOR can submit invoices.\n❌ PROJECT_MANAGER, FINANCE_OFFICER, COMPLIANCE_OFFICER cannot submit invoices.")
    public ResponseEntity<ApiResponseDTO<InvoiceResponseDTO>> submitInvoice(@Valid @RequestBody InvoiceRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success("Invoice submitted successfully", invoiceService.submitInvoice(request)));
    }

    @GetMapping
    @Operation(summary = "Get all invoices [PUBLIC]")
    public ResponseEntity<ApiResponseDTO<List<InvoiceResponseDTO>>> getAllInvoices() {
        return ResponseEntity.ok(ApiResponseDTO.success("Invoices retrieved", invoiceService.getAllInvoices()));
    }

    @GetMapping("/{invoiceId}")
    @Operation(summary = "Get invoice by ID [PUBLIC]")
    public ResponseEntity<ApiResponseDTO<InvoiceResponseDTO>> getInvoiceById(@PathVariable Long invoiceId) {
        return ResponseEntity.ok(ApiResponseDTO.success("Invoice retrieved", invoiceService.getInvoiceById(invoiceId)));
    }

    @GetMapping("/contract/{contractId}")
    @Operation(summary = "Get invoices by contract [PUBLIC]")
    public ResponseEntity<ApiResponseDTO<List<InvoiceResponseDTO>>> getInvoicesByContract(@PathVariable Long contractId) {
        return ResponseEntity.ok(ApiResponseDTO.success("Invoices retrieved", invoiceService.getInvoicesByContract(contractId)));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get invoices by status [PUBLIC]")
    public ResponseEntity<ApiResponseDTO<List<InvoiceResponseDTO>>> getInvoicesByStatus(@PathVariable InvoiceStatus status) {
        return ResponseEntity.ok(ApiResponseDTO.success("Invoices retrieved", invoiceService.getInvoicesByStatus(status)));
    }

    @PatchMapping("/{invoiceId}/approve")
    @PreAuthorize("hasRole('FINANCE_OFFICER')")
    @Operation(summary = "Approve invoice [FINANCE_OFFICER only]",
               description = "✅ Only FINANCE_OFFICER can approve invoices.\n❌ VENDOR, PROJECT_MANAGER, COMPLIANCE_OFFICER, ADMIN cannot approve invoices.")
    public ResponseEntity<ApiResponseDTO<InvoiceResponseDTO>> approveInvoice(@PathVariable Long invoiceId) {
        return ResponseEntity.ok(ApiResponseDTO.success("Invoice approved", invoiceService.approveInvoice(invoiceId)));
    }

    @PatchMapping("/{invoiceId}/reject")
    @PreAuthorize("hasRole('FINANCE_OFFICER')")
    @Operation(summary = "Reject invoice [FINANCE_OFFICER only]",
               description = "✅ Only FINANCE_OFFICER can reject invoices.\n❌ VENDOR cannot reject their own invoice.")
    public ResponseEntity<ApiResponseDTO<InvoiceResponseDTO>> rejectInvoice(
            @PathVariable Long invoiceId,
            @RequestParam String reason) {
        return ResponseEntity.ok(ApiResponseDTO.success("Invoice rejected", invoiceService.rejectInvoice(invoiceId, reason)));
    }

    @DeleteMapping("/{invoiceId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete invoice [ADMIN only]")
    public ResponseEntity<ApiResponseDTO<Void>> deleteInvoice(@PathVariable Long invoiceId) {
        invoiceService.deleteInvoice(invoiceId);
        return ResponseEntity.ok(ApiResponseDTO.success("Invoice deleted"));
    }
}
