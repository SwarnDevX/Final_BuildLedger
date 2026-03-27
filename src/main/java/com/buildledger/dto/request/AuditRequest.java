package com.buildledger.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Data
public class AuditRequest {

    @NotBlank(message = "Scope is required")
    @Size(max = 300, message = "Scope must not exceed 300 characters")
    private String scope;

    @Size(max = 1000, message = "Findings must not exceed 1000 characters")
    private String findings;

    @NotNull(message = "Date is required")
    @PastOrPresent(message = "Date cannot be in the future")
    private LocalDate date;

}
