package com.buildledger.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateVendorRequestDTO {
    @NotBlank(message = "Vendor name is required")
    private String name;
    private String contactInfo;
    @Email(message = "Invalid email format")
    private String email;
    private String phone;
    @NotBlank(message = "Category is required")
    private String category;
    private String address;
}
