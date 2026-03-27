package com.buildledger.dto.request;

import com.buildledger.enums.PaymentMethod;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PaymentRequest {

    @NotNull(message = "Invoice ID is required")
    @Positive(message = "Invoice ID must be positive")
    private Long invoiceId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Digits(integer = 12, fraction = 2, message = "Invalid amount format")
    private BigDecimal amount;

    @NotNull(message = "Payment date is required")
    @PastOrPresent(message = "Payment date cannot be in the future")
    private LocalDate date;

    @NotNull(message = "Payment method is required")
    private PaymentMethod method;

    @NotBlank(message = "Transaction reference is required")
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "Transaction reference must be alphanumeric")
    private String transactionReference;


    @Size(max = 500, message = "Remarks too long")
    private String remarks;
}
