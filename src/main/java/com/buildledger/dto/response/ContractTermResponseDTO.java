package com.buildledger.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ContractTermResponseDTO {
    private Long termId;
    private Long contractId;
    private String description;
    private Boolean complianceFlag;
    private Integer sequenceNumber;
    private LocalDateTime createdAt;
}
