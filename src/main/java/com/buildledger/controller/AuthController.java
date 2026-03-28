package com.buildledger.controller;

import com.buildledger.dto.request.LoginRequestDTO;
import com.buildledger.dto.response.ApiResponseDTO;
import com.buildledger.dto.response.LoginResponseDTO;
import com.buildledger.entity.User;
import com.buildledger.exception.ResourceNotFoundException;
import com.buildledger.repository.UserRepository;
import com.buildledger.service.AuthService;
import com.buildledger.service.VendorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Login and session info")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final VendorService vendorService;

    @PostMapping("/login")
    @Operation(summary = "Login",
            description = "Authenticate with username and password to receive JWT token.\n\n" +
                    "**Default credentials:** username=`admin12`, password=`Admin@123`\n\n" +
                    "After login, copy the `accessToken` and click **Authorize 🔒** at the top → enter `Bearer <token>`")
    public ResponseEntity<ApiResponseDTO<LoginResponseDTO>> login(@Valid @RequestBody LoginRequestDTO request) {
        log.info("Login request for user: {}", request.getUsername());
        LoginResponseDTO response = authService.login(request);
        return ResponseEntity.ok(ApiResponseDTO.success("Login successful", response));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Who am I? — Current logged-in user",
            description = "🔍 Shows who is currently logged in — name, username, role, status.\n\n" +
                    "Use this to confirm which user is active after clicking **Authorize 🔒**.\n\n" +
                    "**Try this first after logging in to confirm your session!**")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("userId", user.getUserId());
        info.put("username", user.getUsername());
        info.put("name", user.getName());
        info.put("role", user.getRole());
        info.put("email", user.getEmail());
        info.put("status", user.getStatus());
        info.put("message", "You are logged in as " + user.getRole() + " (" + user.getName() + ")");

        return ResponseEntity.ok(ApiResponseDTO.success(
                "✅ Logged in as: " + user.getName() + " [" + user.getRole() + "]", info));
    }

    // ── Vendor password change ────────────────────────────────────────────────

    @PutMapping("/vendor/change-password")
    @PreAuthorize("hasRole('VENDOR')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Change vendor password [VENDOR only — must be verified/ACTIVE]",
            description = """
            After an admin approves your documents, a **temporary password** is printed
            in the server console. Use that temp password to login, then call this endpoint
            to set your own permanent password.

            **Rules:**
            - Only authenticated vendors with role `VENDOR` can call this.
            - Your vendor account must be in **ACTIVE** status.
            - `oldPassword` must match your current password.
            - `newPassword` must be at least 8 characters and different from the old one.

            **Steps:**
            1. Admin approves your documents → temp password printed in server console.
            2. Login via `POST /auth/login` using your username + temp password.
            3. Call this endpoint with your temp password as `oldPassword` and your chosen password as `newPassword`.
            """
    )
    public ResponseEntity<ApiResponseDTO<Void>> changeVendorPassword(
            Authentication authentication,
            @RequestParam
            @NotBlank(message = "Current password is required")
            String oldPassword,
            @RequestParam
            @NotBlank(message = "New password is required")
            @Size(min = 8, message = "New password must be at least 8 characters")
            String newPassword) {

        vendorService.changeVendorPassword(authentication.getName(), oldPassword, newPassword);
        return ResponseEntity.ok(ApiResponseDTO.success("Password changed successfully. Use your new password for future logins."));
    }

    // ── Vendor username update ────────────────────────────────────────────────

    @PutMapping("/vendor/update-username")
    @PreAuthorize("hasRole('VENDOR')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Update vendor username [VENDOR only — must be verified/ACTIVE]",
            description = """
            Allows a verified vendor to change their auto-assigned username to one of their choice.

            **Rules:**
            - Only authenticated vendors with role `VENDOR` can call this.
            - Your vendor account must be in **ACTIVE** status.
            - The new username must be unique — if it is already taken, you will receive a clear error.
            - Username is stored in lowercase and trimmed of whitespace.

            **Note:** After updating your username, you must log in again using the new username.
            """
    )
    public ResponseEntity<ApiResponseDTO<Void>> updateVendorUsername(
            Authentication authentication,
            @RequestParam
            @NotBlank(message = "New username is required")
            @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
            String newUsername) {

        vendorService.updateVendorUsername(authentication.getName(), newUsername);
        return ResponseEntity.ok(ApiResponseDTO.success("Username updated successfully. Please log in again with your new username: " + newUsername.trim().toLowerCase()));
    }
}
