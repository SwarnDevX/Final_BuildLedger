package com.buildledger.dto.response;

import com.buildledger.enums.AuditStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AuditResponse {
    private Long auditId;
    private Long complianceOfficerId;
    private String officerName;
    private String scope;
    private String findings;
    private LocalDate date;
    private LocalDate auditDate;
    private AuditStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
