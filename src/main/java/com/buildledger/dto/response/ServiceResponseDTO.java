package com.buildledger.dto.response;

import com.buildledger.enums.ServiceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ServiceResponseDTO {
    private Long serviceId;
    private Long contractId;
    private String description;
    private LocalDate completionDate;
    private ServiceStatus status;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
