package com.buildledger.dto.response;

import com.buildledger.enums.InvoiceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class InvoiceResponseDTO {
    private Long invoiceId;
    private Long contractId;
    private String contractVendorName;
    private BigDecimal amount;
    private LocalDate date;
    private LocalDate dueDate;
    private String description;
    private InvoiceStatus status;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
