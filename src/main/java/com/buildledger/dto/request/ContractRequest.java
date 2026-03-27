package com.buildledger.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ContractRequest {

    @NotNull(message = "Vendor ID is required")
    private Long vendorId;

    @NotNull(message = "Project ID is required")
    private Long projectId;

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date cannot be in the past")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be greater than the start date")
    private LocalDate endDate;

    @NotNull(message = "Contract value is required")
    @Positive(message = "Contract value must be positive")
    private BigDecimal value;

    @Size(max = 500, message = "Description too long")
    private String description;
}
