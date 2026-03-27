package com.buildledger.dto.response;

import com.buildledger.enums.ComplianceStatus;
import com.buildledger.enums.ComplianceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ComplianceRecordResponse {
    private Long complianceId;
    private Long contractId;
    private ComplianceType type;
    private String result;
    private LocalDate date;
    private String notes;
    private ComplianceStatus status;
    private LocalDateTime createdAt;
}
