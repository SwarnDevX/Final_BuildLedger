package com.buildledger.dto.request;

import com.buildledger.enums.VendorStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for partial-update of a Vendor.
 *
 * <p>All fields are optional. The service layer applies only the non-null
 * fields and validates status transitions against the state machine.
 *
 * <p>FIXES applied:
 * <ul>
 *   <li>Removed {@code @NotBlank} from {@code category} — it is optional on update.</li>
 *   <li>Removed {@code @NotNull} from {@code status} — callers must not be forced
 *       to supply a status on every update; the service handles null gracefully.</li>
 * </ul>
 */
@Data
public class UpdateVendorRequestDTO {

    @Size(min = 2, max = 100, message = "Vendor name must be between 2 and 100 characters")
    private String name;

    @Size(max = 200, message = "Contact info cannot exceed 200 characters")
    private String contactInfo;

    @Email(message = "Invalid email format")
    private String email;

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Phone number must be a valid 10-digit Indian mobile number starting with 6-9")
    private String phone;

    // No @NotBlank — this is a partial update; category is optional
    @Size(max = 100, message = "Category cannot exceed 100 characters")
    private String category;

    @Size(max = 300, message = "Address cannot exceed 300 characters")
    private String address;

    /**
     * Optional. If provided, the service validates that the transition from
     * the current status to this value is permitted by the VendorStatus state machine.
     *
     * <p>Allowed transitions (enforced in {@code VendorServiceImpl}):
     * <pre>
     *   PENDING     → ACTIVE | SUSPENDED
     *   ACTIVE      → SUSPENDED | BLACKLISTED
     *   SUSPENDED   → ACTIVE | BLACKLISTED
     *   BLACKLISTED → (terminal — no transitions allowed)
     * </pre>
     */
    private VendorStatus status;
}
