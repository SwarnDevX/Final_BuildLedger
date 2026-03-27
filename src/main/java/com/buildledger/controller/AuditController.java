package com.buildledger.controller;

import com.buildledger.dto.request.AuditRequest;
import com.buildledger.dto.response.ApiResponse;
import com.buildledger.dto.response.AuditResponse;
import com.buildledger.dto.response.ComplianceRecordResponse;
import com.buildledger.enums.AuditStatus;
import com.buildledger.enums.ComplianceStatus;
import com.buildledger.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Audit", description = "Audiding Vendor Records")
@SecurityRequirement(name = "bearerAuth")
public class AuditController {

    private final AuditService auditService;

    @PostMapping("/audits")
    @PreAuthorize("hasRole('COMPLIANCE_OFFICER') or hasRole('ADMIN')")
    @Operation(summary = "Create audit", description = "COMPLIANCE_OFFICER/ADMIN: Schedule a new audit")
    public ResponseEntity<ApiResponse<AuditResponse>> createAudit(
            @Valid @RequestBody AuditRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Audit created",
                        auditService.createAudit(request, authentication.getName())));
    }

    @GetMapping("/audits")
    @PreAuthorize("hasRole('COMPLIANCE_OFFICER') or hasRole('ADMIN')")
    @Operation(summary = "Get all audits", description = "PUBLIC")
    public ResponseEntity<ApiResponse<List<AuditResponse>>> getAllAudits() {
        return ResponseEntity.ok(ApiResponse.success("Audits retrieved", auditService.getAllAudits()));
    }

    @GetMapping("/audits/{auditId}")
    @PreAuthorize("hasRole('COMPLIANCE_OFFICER') or hasRole('ADMIN')")
    @Operation(summary = "Get audit by ID")
    public ResponseEntity<ApiResponse<AuditResponse>> getAuditById(@PathVariable Long auditId) {
        return ResponseEntity.ok(ApiResponse.success("Audit retrieved", auditService.getAuditById(auditId)));
    }

    @PatchMapping("/audits/{auditId}/status")
    @PreAuthorize("hasRole('COMPLIANCE_OFFICER') or hasRole('ADMIN')")
    @Operation(summary = "Update audit status and findings", description = "COMPLIANCE_OFFICER/ADMIN: Progress audit lifecycle")
    public ResponseEntity<ApiResponse<AuditResponse>> updateAuditStatus(
            @PathVariable Long auditId,
            @RequestParam AuditStatus status,
            @RequestParam(required = false) String findings) {
        return ResponseEntity.ok(ApiResponse.success("Audit updated",
                auditService.updateAuditStatus(auditId, status, findings)));
    }

}
