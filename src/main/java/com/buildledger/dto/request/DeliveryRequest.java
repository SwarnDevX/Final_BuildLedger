package com.buildledger.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DeliveryRequest {

    @NotNull(message = "Contract ID is required")
    @Positive(message = "Contract ID must be positive")
    private Long contractId;

    @NotNull(message = "Delivery date is required")
    @FutureOrPresent(message = "Delivery date cannot be in the past")
    private LocalDate date;

    @NotBlank(message = "Item is required")
    @Size(max = 150, message = "Item name too long")
    private String item;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.01", message = "Quantity must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "Invalid quantity format")
    private BigDecimal quantity;

    @NotBlank(message = "Unit is required")
    @Pattern(regexp = "^(KG|LITRE|PCS|TON)$", message = "Invalid unit")
    private String unit;

    @Size(max = 500, message = "Remarks too long")
    private String remarks;
}
