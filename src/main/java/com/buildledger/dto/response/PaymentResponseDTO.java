package com.buildledger.dto.response;

import com.buildledger.enums.PaymentMethod;
import com.buildledger.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PaymentResponseDTO {
    private Long paymentId;
    private Long invoiceId;
    private BigDecimal amount;
    private LocalDate date;
    private PaymentMethod method;
    private PaymentStatus status;
    private String transactionReference;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
