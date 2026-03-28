package com.buildledger.dto.response;

import com.buildledger.enums.VendorStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class VendorResponseDTO {
    private Long vendorId;
    private String name;
    private String contactInfo;
    private String email;
    private String phone;
    private String category;
    private String address;
    private VendorStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long userId;
}
