package com.buildledger.dto.response;

import com.buildledger.enums.DocumentType;
import com.buildledger.enums.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class VendorDocumentResponseDTO {
    private Long documentId;
    private Long vendorId;
    private String vendorName;
    private DocumentType docType;
    private String fileUri;
    private LocalDate uploadedDate;
    private VerificationStatus verificationStatus;
    private String remarks;
    private String reviewedBy;
    private LocalDateTime reviewedAt;
    private String reviewRemarks;
    private LocalDateTime createdAt;
}
