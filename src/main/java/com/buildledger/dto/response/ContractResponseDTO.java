package com.buildledger.dto.response;

import com.buildledger.enums.ContractStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ContractResponseDTO {
    private Long contractId;
    private Long vendorId;
    private String vendorName;
    private Long projectId;
    private String projectName;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal value;
    private ContractStatus status;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
