package com.buildledger.dto.response;

import com.buildledger.enums.DeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DeliveryResponseDTO {
    private Long deliveryId;
    private Long contractId;
    private LocalDate date;
    private String item;
    private BigDecimal quantity;
    private String unit;
    private String remarks;
    private DeliveryStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
