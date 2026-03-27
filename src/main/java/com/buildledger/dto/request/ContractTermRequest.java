package com.buildledger.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ContractTermRequest {

    @NotBlank(message = "Description is required")
    @Size(max = 300)
    private String description;

    private Boolean complianceFlag = false;

    @Positive(message = "Sequence must be positive")
    private Integer sequenceNumber;
}
