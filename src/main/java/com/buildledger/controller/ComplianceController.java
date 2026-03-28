package com.buildledger.controller;

import com.buildledger.dto.request.ComplianceRecordRequestDTO;
import com.buildledger.dto.response.ApiResponseDTO;
import com.buildledger.dto.response.ComplianceRecordResponseDTO;
import com.buildledger.service.ComplianceService;
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
@RequiredArgsConstructor
@Tag(name = "Compliance & Audit", description = "Compliance monitoring and audit management")
@SecurityRequirement(name = "bearerAuth")
public class ComplianceController {

    private final ComplianceService complianceService;

    @PostMapping("/compliance")
    @PreAuthorize("hasRole('COMPLIANCE_OFFICER') or hasRole('ADMIN')")
    @Operation(summary = "Create compliance record", description = "COMPLIANCE_OFFICER/ADMIN: Record a compliance check")
    public ResponseEntity<ApiResponseDTO<ComplianceRecordResponseDTO>> createComplianceRecord(
            @Valid @RequestBody ComplianceRecordRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success("Compliance record created",
                        complianceService.createComplianceRecord(request)));
    }

    @GetMapping("/compliance")
    @Operation(summary = "Get all compliance records", description = "PUBLIC")
    public ResponseEntity<ApiResponseDTO<List<ComplianceRecordResponseDTO>>> getAllComplianceRecords() {
        return ResponseEntity.ok(ApiResponseDTO.success("Compliance records retrieved",
                complianceService.getAllComplianceRecords()));
    }

    @GetMapping("/compliance/{complianceId}")
    @Operation(summary = "Get compliance record by ID")
    public ResponseEntity<ApiResponseDTO<ComplianceRecordResponseDTO>> getComplianceRecordById(
            @PathVariable Long complianceId) {
        return ResponseEntity.ok(ApiResponseDTO.success("Compliance record retrieved",
                complianceService.getComplianceRecordById(complianceId)));
    }

    @GetMapping("/compliance/contract/{contractId}")
    @Operation(summary = "Get compliance records by contract")
    public ResponseEntity<ApiResponseDTO<List<ComplianceRecordResponseDTO>>> getComplianceRecordsByContract(
            @PathVariable Long contractId) {
        return ResponseEntity.ok(ApiResponseDTO.success("Compliance records retrieved",
                complianceService.getComplianceRecordsByContract(contractId)));
    }
}
