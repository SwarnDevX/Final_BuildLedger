package com.buildledger.controller;

import com.buildledger.dto.request.CreateUserRequestDTO;
import com.buildledger.dto.request.UpdateUserRequestDTO;
import com.buildledger.dto.response.ApiResponseDTO;
import com.buildledger.dto.response.UserResponseDTO;
import com.buildledger.enums.Role;
import com.buildledger.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "Admin operations for managing users")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create user", description = "ADMIN only: Create a new user with any role")
    public ResponseEntity<ApiResponseDTO<UserResponseDTO>> createUser(@Valid @RequestBody CreateUserRequestDTO request) {
        UserResponseDTO user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success("User created successfully", user));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "ADMIN only: Retrieve all users")
    public ResponseEntity<ApiResponseDTO<List<UserResponseDTO>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponseDTO.success("Users retrieved", userService.getAllUsers()));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponseDTO<UserResponseDTO>> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponseDTO.success("User retrieved", userService.getUserById(userId)));
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get users by role", description = "ADMIN only: Filter users by role")
    public ResponseEntity<ApiResponseDTO<List<UserResponseDTO>>> getUsersByRole(@PathVariable Role role) {
        return ResponseEntity.ok(ApiResponseDTO.success("Users retrieved", userService.getUsersByRole(role)));
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user", description = "ADMIN only: Update user details")
    public ResponseEntity<ApiResponseDTO<UserResponseDTO>> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequestDTO request) {
        return ResponseEntity.ok(ApiResponseDTO.success("User updated successfully", userService.updateUser(userId, request)));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user", description = "ADMIN only: Delete a user")
    public ResponseEntity<ApiResponseDTO<Void>> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponseDTO.success("User deleted successfully"));
    }
}
