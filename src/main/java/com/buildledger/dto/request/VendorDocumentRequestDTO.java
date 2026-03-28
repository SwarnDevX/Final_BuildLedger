package com.buildledger.dto.request;

import com.buildledger.enums.DocumentType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VendorDocumentRequestDTO {

    @NotNull(message = "Document type is required")
    private DocumentType docType;

    private String remarks;
}
