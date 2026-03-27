package com.buildledger.controller;

import com.buildledger.dto.request.AuditRequest;
import com.buildledger.dto.request.ComplianceRecordRequest;
import com.buildledger.dto.response.ApiResponse;
import com.buildledger.dto.response.AuditResponse;
import com.buildledger.dto.response.ComplianceRecordResponse;
import com.buildledger.enums.AuditStatus;
import com.buildledger.service.ComplianceService;
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
@Tag(name = "Compliance & Audit", description = "Compliance monitoring and audit management")
@SecurityRequirement(name = "bearerAuth")
public class ComplianceController {

    private final ComplianceService complianceService;

    @PostMapping("/compliance")
    @PreAuthorize("hasRole('COMPLIANCE_OFFICER') or hasRole('ADMIN')")
    @Operation(summary = "Create compliance record", description = "COMPLIANCE_OFFICER/ADMIN: Record a compliance check")
    public ResponseEntity<ApiResponse<ComplianceRecordResponse>> createComplianceRecord(
            @Valid @RequestBody ComplianceRecordRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Compliance record created",
                        complianceService.createComplianceRecord(request)));
    }

    @GetMapping("/compliance")
    @Operation(summary = "Get all compliance records", description = "PUBLIC")
    public ResponseEntity<ApiResponse<List<ComplianceRecordResponse>>> getAllComplianceRecords() {
        return ResponseEntity.ok(ApiResponse.success("Compliance records retrieved",
                complianceService.getAllComplianceRecords()));
    }

    @GetMapping("/compliance/{complianceId}")
    @Operation(summary = "Get compliance record by ID")
    public ResponseEntity<ApiResponse<ComplianceRecordResponse>> getComplianceRecordById(
            @PathVariable Long complianceId) {
        return ResponseEntity.ok(ApiResponse.success("Compliance record retrieved",
                complianceService.getComplianceRecordById(complianceId)));
    }

    @GetMapping("/compliance/contract/{contractId}")
    @Operation(summary = "Get compliance records by contract")
    public ResponseEntity<ApiResponse<List<ComplianceRecordResponse>>> getComplianceRecordsByContract(
            @PathVariable Long contractId) {
        return ResponseEntity.ok(ApiResponse.success("Compliance records retrieved",
                complianceService.getComplianceRecordsByContract(contractId)));
    }
}
