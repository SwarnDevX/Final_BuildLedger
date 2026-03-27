package com.buildledger.dto.request;

import com.buildledger.enums.ComplianceType;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ComplianceRecordRequest {

    @NotNull(message = "Contract ID cannot be null")
    @Positive(message = "Contract ID must be a positive number")
    private Long contractId;

    @NotNull(message = "Compliance type is required")
    private ComplianceType type;

    @NotBlank(message = "Result is required")
    private String result;

    @NotNull(message = "Compliance date is required")
    @FutureOrPresent(message = "Date must be today or later")
    private LocalDate date;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
}
