package com.buildledger.controller;

import com.buildledger.dto.response.ApiResponseDTO;
import com.buildledger.entity.AuditLog;
import com.buildledger.repository.AuditLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "System-wide immutable audit trail (ADMIN only)")
@SecurityRequirement(name = "bearerAuth")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all audit logs", description = "ADMIN only: View complete system audit trail")
    public ResponseEntity<ApiResponseDTO<List<AuditLog>>> getAllAuditLogs() {
        return ResponseEntity.ok(ApiResponseDTO.success("Audit logs retrieved", auditLogRepository.findAll()));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get audit logs by user")
    public ResponseEntity<ApiResponseDTO<List<AuditLog>>> getAuditLogsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponseDTO.success("Audit logs retrieved",
                auditLogRepository.findByUserId(userId)));
    }
}
