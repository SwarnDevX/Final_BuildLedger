package com.buildledger.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;


@Data
public class InvoiceRequest {

    @NotNull(message = "Contract ID is required")
    @Positive(message = "Contract ID must be positive")
    private Long contractId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Digits(integer = 12, fraction = 2, message = "Invalid amount format")
    private BigDecimal amount;

    @NotNull(message = "Invoice date is required")
    @PastOrPresent(message = "Invoice date cannot be in the future")
    private LocalDate date;

    @Future(message = "Due date must be in the future")
    private LocalDate dueDate;

    @Size(max = 500, message = "Description too long")
    private String description;
}