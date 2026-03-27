package com.buildledger.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ServiceRequest {

    @NotNull(message = "Contract ID is required")
    @Positive(message = "Contract ID must be a positive number")
    private Long contractId;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 300, message = "Description must be between 10 and 300 characters")
    private String description;

    @NotNull(message = "Completion date is required")
    @FutureOrPresent(message = "Completion date cannot be in the past")
    private LocalDate completionDate;

    @Size(max = 250, message = "Remarks cannot exceed 250 characters")
    private String remarks;
}
