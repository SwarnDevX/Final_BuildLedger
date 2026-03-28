package com.buildledger.service.impl;

import com.buildledger.dto.request.CreateUserRequestDTO;
import com.buildledger.dto.request.UpdateUserRequestDTO;
import com.buildledger.dto.response.UserResponseDTO;
import com.buildledger.entity.User;
import com.buildledger.enums.Role;
import com.buildledger.exception.BadRequestException;
import com.buildledger.exception.DuplicateResourceException;
import com.buildledger.exception.InvalidRoleAssignmentException;
import com.buildledger.exception.ResourceNotFoundException;
import com.buildledger.repository.UserRepository;
import com.buildledger.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Service-layer password policy.
     * Mirrors the DTO @Pattern as a defence-in-depth check — catches calls
     * that bypass the controller (direct service injection in tests, internal calls).
     */
//    private static final Pattern PASSWORD_POLICY =
//            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,100}$");

    // ── Create ────────────────────────────────────────────────────────────────

    @Override
    public UserResponseDTO createUser(CreateUserRequestDTO request) {
        log.info("Creating user: {} with role: {}", request.getUsername(), request.getRole());

        // ── GUARD 1: Role assignment — ADMIN and VENDOR are privileged roles ──
        // ADMIN:  provisioned via AdminBootstrap only.
        // VENDOR: automatically assigned by VendorServiceImpl.approveVendor().
        //         Allowing direct assignment here would completely bypass the
        //         document-verification workflow.
        if (request.getRole() == Role.ADMIN || request.getRole() == Role.VENDOR) {
            throw new InvalidRoleAssignmentException(request.getRole().name());
        }

        // ── GUARD 2: Duplicate username / email ───────────────────────────────
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already exists: " + request.getUsername());
        }
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        // ── GUARD 3: Password complexity (defence-in-depth) ───────────────────
        validatePasswordComplexity(request.getPassword());

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .role(request.getRole())
                .email(request.getEmail())
                .phone(request.getPhone())
                .build();

        User saved = userRepository.save(user);
        log.info("User created — id={}, username={}, role={}", saved.getUserId(), saved.getUsername(), saved.getRole());
        return mapToResponse(saved);
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long userId) {
        return mapToResponse(findById(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserByUsername(String username) {
        return mapToResponse(
                userRepository.findByUsername(username)
                        .orElseThrow(() -> new ResourceNotFoundException("User", "username", username))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getUsersByRole(Role role) {
        return userRepository.findByRole(role).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ── Update ────────────────────────────────────────────────────────────────

    @Override
    public UserResponseDTO updateUser(Long userId, UpdateUserRequestDTO request) {
        log.info("Updating user id={}", userId);
        User user = findById(userId);

        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getEmail() != null) {
            // Only check duplicate if a NEW email is being set
            if (!request.getEmail().equalsIgnoreCase(user.getEmail())
                    && userRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("Email already registered: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }

        return mapToResponse(userRepository.save(user));
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Override
    public void deleteUser(Long userId) {
        log.info("Deleting user id={}", userId);
        User user = findById(userId);
        userRepository.delete(user);
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    /**
     * Package-visible so {@code VendorServiceImpl} can call it when creating
     * the vendor's user account during approval — keeps password policy
     * enforcement in one place.
     */
    static void validatePasswordComplexity(String password) {
        if (password == null) {
            throw new BadRequestException(
                    "Password must be 8–100 characters and include at least one uppercase letter, " +
                            "one lowercase letter, one digit, and one special character (@$!%*?&).");
        }
    }

    private User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    private UserResponseDTO mapToResponse(User user) {
        return UserResponseDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .name(user.getName())
                .role(user.getRole())
                .email(user.getEmail())
                .phone(user.getPhone())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
