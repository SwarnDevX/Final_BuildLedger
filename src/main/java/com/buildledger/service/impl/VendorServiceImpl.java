package com.buildledger.service.impl;

import com.buildledger.dto.request.CreateVendorRequestDTO;
import com.buildledger.dto.request.UpdateVendorRequestDTO;
import com.buildledger.dto.response.VendorDocumentResponseDTO;
import com.buildledger.dto.response.VendorResponseDTO;
import com.buildledger.entity.User;
import com.buildledger.entity.Vendor;
import com.buildledger.entity.VendorDocument;
import com.buildledger.enums.DocumentType;
import com.buildledger.enums.Role;
import com.buildledger.enums.UserStatus;
import com.buildledger.enums.VendorStatus;
import com.buildledger.enums.VerificationStatus;
import com.buildledger.exception.BadRequestException;
import com.buildledger.exception.DuplicateResourceException;
import com.buildledger.exception.InvalidStatusTransitionException;
import com.buildledger.exception.ResourceNotFoundException;
import com.buildledger.repository.UserRepository;
import com.buildledger.repository.VendorDocumentRepository;
import com.buildledger.repository.VendorRepository;
import com.buildledger.service.FileStorageService;
import com.buildledger.service.VendorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VendorServiceImpl implements VendorService {

    private final VendorRepository         vendorRepository;
    private final VendorDocumentRepository vendorDocumentRepository;
    private final UserRepository           userRepository;
    private final PasswordEncoder          passwordEncoder;
    private final FileStorageService       fileStorageService;

    @Value("${app.document.max-size-mb:10}")
    private long maxFileSizeMb;

    private static boolean isValidStatusTransition(VendorStatus from, VendorStatus to) {
        return switch (from) {
            case PENDING-> to == VendorStatus.ACTIVE    || to == VendorStatus.SUSPENDED;
            case ACTIVE-> to == VendorStatus.SUSPENDED;
            case SUSPENDED-> to == VendorStatus.ACTIVE ;

        };
    }

    /**
     * PUBLIC self-registration endpoint — vendor registers themselves.
     * Identical business logic to createVendor; separated so the controller
     * can expose it without authentication.
     */
    @Override
    public VendorResponseDTO registerVendor(CreateVendorRequestDTO request) {
        log.info("Vendor self-registration: {}", request.getName());
        return createVendor(request);
    }

    @Override
    public VendorResponseDTO createVendor(CreateVendorRequestDTO request) {
        log.info("Registering vendor: {}", request.getName());

        if (vendorRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Vendor email already registered: " + request.getEmail());
        }

        Vendor vendor = Vendor.builder()
                .name(request.getName())
                .contactInfo(request.getContactInfo())
                .email(request.getEmail())
                .phone(request.getPhone())
                .category(request.getCategory())
                .address(request.getAddress())
                // status defaults to PENDING via @Builder.Default
                .build();

        return mapToResponse(vendorRepository.save(vendor));
    }

    @Override
    @Transactional(readOnly = true)
    public VendorResponseDTO getVendorById(Long vendorId) {
        return mapToResponse(findVendorById(vendorId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VendorResponseDTO> getAllVendors() {
        return vendorRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VendorResponseDTO> getVendorsByStatus(VendorStatus status) {
        return vendorRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public VendorResponseDTO updateVendor(Long vendorId, UpdateVendorRequestDTO request) {
        Vendor vendor = findVendorById(vendorId);

        if (request.getName()        != null) vendor.setName(request.getName());
        if (request.getContactInfo() != null) vendor.setContactInfo(request.getContactInfo());
        if (request.getPhone()       != null) vendor.setPhone(request.getPhone());
        if (request.getCategory()    != null) vendor.setCategory(request.getCategory());
        if (request.getAddress()     != null) vendor.setAddress(request.getAddress());

        if (request.getEmail() != null) {
            if (!request.getEmail().equalsIgnoreCase(vendor.getEmail())
                    && vendorRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("Email already registered: " + request.getEmail());
            }
            vendor.setEmail(request.getEmail());
        }

        // ── Status transition validation ──────────────────────────────────────
        if (request.getStatus() != null) {
            VendorStatus current = vendor.getStatus();
            VendorStatus next    = request.getStatus();

            if (current == next) {
                throw new BadRequestException("Vendor is already in " + current + " status.");
            }
            if (!isValidStatusTransition(current, next)) {
                throw new InvalidStatusTransitionException(
                        current.name(), next.name(),
                        "Allowed transitions from " + current + ": " + allowedTransitions(current)
                );
            }
            vendor.setStatus(next);
            log.info("Vendor {} status manually changed: {} → {}", vendorId, current, next);
        }

        return mapToResponse(vendorRepository.save(vendor));
    }

    @Override
    public void deleteVendor(Long vendorId) {
        Vendor vendor = findVendorById(vendorId);

        // Clean up stored PDF files from disk before deleting the vendor
        List<VendorDocument> docs = vendorDocumentRepository.findByVendorVendorId(vendorId);
        docs.forEach(doc -> fileStorageService.delete(doc.getFileUri()));

        vendorRepository.delete(vendor);
    }

    // ── Document lifecycle ────────────────────────────────────────────────────

    @Override
    public VendorDocumentResponseDTO uploadDocument(Long vendorId, MultipartFile file,
                                                    DocumentType docType, String remarks,
                                                    String uploaderUsername) {
        log.info("Document upload: vendorId={}, docType={}, uploader={}", vendorId, docType, uploaderUsername);

        Vendor vendor = findVendorById(vendorId);

        if(vendor.getStatus()!=VendorStatus.PENDING){
            throw new BadRequestException(
                    "Document upload is not allowed. Vendor status is "+vendor.getStatus()+". Only vendors in Pending status can upload documents."
            );
        }

        if(!vendorDocumentRepository.findByVendorVendorId(vendorId).isEmpty()){
            throw new BadRequestException(
                    "A document has already been uploaded for this vendor. "+"Only one document upload is allowed per vendor."
            );
        }

//         ── Remarks validation ────────────────────────────────────────────────
//        if (remarks == null || remarks.isBlank()) {
//            throw new BadRequestException("Remarks are required when uploading a document.");
//        }
//        if (remarks.trim().length() < 5) {
//            throw new BadRequestException("Remarks must be at least 5 characters.");
//        }

        // ── File validation ───────────────────────────────────────────────────
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("No file provided. Please attach a PDF file.");
        }
        String originalName = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "document.pdf");
        if (!originalName.toLowerCase().endsWith(".pdf")) {
            throw new BadRequestException("Only PDF files are accepted. Got: " + originalName);
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equalsIgnoreCase("application/pdf")) {
            throw new BadRequestException(
                    "Invalid content type '" + contentType + "'. Only application/pdf is allowed.");
        }
        long maxBytes = maxFileSizeMb * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new BadRequestException("File size " + (file.getSize() / (1024 * 1024)) +
                    " MB exceeds the allowed limit of " + maxFileSizeMb + " MB.");
        }

        String fileUri = fileStorageService.store(file, vendorId);
        VendorDocument document = VendorDocument.builder()
                .vendor(vendor)
                .docType(docType)
                .fileUri(fileUri)
                .uploadedDate(LocalDate.now())
                .verificationStatus(VerificationStatus.PENDING)
                .remarks(remarks)
                .build();

        VendorDocument saved = vendorDocumentRepository.save(document);
        log.info("Document saved: id={}, path={}", saved.getDocumentId(), fileUri);
        return mapDocumentToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VendorDocumentResponseDTO> getVendorDocuments(Long vendorId) {
        findVendorById(vendorId); // ensures vendor exists
        return vendorDocumentRepository.findByVendorVendorId(vendorId).stream()
                .map(this::mapDocumentToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VendorDocumentResponseDTO> getDocumentsByStatus(VerificationStatus status) {
        return vendorDocumentRepository.findByVerificationStatus(status).stream()
                .map(this::mapDocumentToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Resource downloadDocument(Long documentId) {
        VendorDocument document = findDocumentById(documentId);
        return fileStorageService.load(document.getFileUri());
    }

    @Override
    public VendorDocumentResponseDTO reviewDocument(Long documentId, VerificationStatus status,
                                                    String reviewRemarks, String reviewerUsername) {
        log.info("Document review: id={}, newStatus={}, reviewer={}", documentId, status, reviewerUsername);

        if (status == VerificationStatus.PENDING) {
            throw new BadRequestException("Review result must be APPROVED or REJECTED — not PENDING.");
        }
//        if (reviewRemarks == null || reviewRemarks.isBlank() || reviewRemarks.trim().length() < 5) {
//            throw new BadRequestException("Review remarks are required (minimum 5 characters).");
//        }

        VendorDocument document = findDocumentById(documentId);
        VerificationStatus current = document.getVerificationStatus();

        if (current != VerificationStatus.PENDING) {
            throw new BadRequestException(
                    "Document is already " + current + " and cannot be reviewed again. " +
                            "Once a document has been APPROVED or REJECTED the decision is final.");
        }

        document.setVerificationStatus(status);
        document.setReviewedBy(reviewerUsername);
        document.setReviewedAt(LocalDateTime.now());
        document.setReviewRemarks(reviewRemarks);
        vendorDocumentRepository.save(document);

        // Trigger status recalculation after every review
        autoUpdateVendorStatus(document.getVendor().getVendorId());

        return mapDocumentToResponse(document);
    }

    // ── Vendor → User approval flow ───────────────────────────────────────────

    /**
     * Recalculates the vendor's status after each document review.
     *
     * <p><b>CRITICAL DESIGN RULE:</b> When all documents are APPROVED and the
     * vendor transitions to ACTIVE for the first time, a {@link User} account
     * is created with {@code role = VENDOR}. The new user's ID is stored on
     * the {@link Vendor} entity. After this point, all operational actions
     * (login, invoice submission) MUST use {@code vendor.getUserId()}, NOT
     * {@code vendor.getVendorId()}.
     *
     * <p>Idempotency: if {@code vendor.getUserId()} is already set, user
     * creation is skipped — calling this method multiple times is safe.
     */
    @Override
    public void autoUpdateVendorStatus(Long vendorId) {
        Vendor vendor = vendorRepository.findById(vendorId).orElse(null);
        if (vendor == null) return;

        // Only auto-update while still PENDING — manual status changes are respected
        if (vendor.getStatus() != VendorStatus.PENDING) return;

        List<VendorDocument> documents = vendorDocumentRepository.findByVendorVendorId(vendorId);
        if (documents.isEmpty()) return;

        boolean allApproved = documents.stream()
                .allMatch(d -> d.getVerificationStatus() == VerificationStatus.APPROVED);
        boolean anyRejected = documents.stream()
                .anyMatch(d -> d.getVerificationStatus() == VerificationStatus.REJECTED);

        if (allApproved) {
            vendor.setStatus(VendorStatus.ACTIVE);
            vendorRepository.save(vendor);
            log.info("Vendor {} auto-promoted to ACTIVE (all documents APPROVED)", vendorId);

            // ── Create the User account for this newly ACTIVE vendor ───────────
            createVendorUserAccount(vendor);

        } else if (anyRejected) {
            // Documents rejected — vendor stays PENDING, userId remains null.
            // They are NOT added to the users table. Admin can review manually.
            vendor.setStatus(VendorStatus.SUSPENDED);
            vendor.setUserId(null); // Ensure userId is null — never enters users table
            vendorRepository.save(vendor);
            log.info("Vendor {} suspended (one or more documents REJECTED). userId remains null.", vendorId);
        }
    }

    /**
     * Creates a {@link User} entry for an approved vendor.
     *
     * <p><b>Idempotency guard:</b> if {@code vendor.getUserId()} is already
     * non-null, a user account exists and we skip creation. This prevents
     * duplicate accounts if {@code autoUpdateVendorStatus} is ever called
     * concurrently or multiple times (the {@code @Version} on Vendor provides
     * the optimistic-lock safety net at the DB level).
     *
     * <p><b>Credentials generated:</b>
     * <ul>
     *   <li>Username: {@code vendor_<vendorId>_<emailLocalPart>}</li>
     *   <li>Password: random UUID — the vendor MUST reset via a
     *       password-reset flow on first login.</li>
     * </ul>
     */
    private void createVendorUserAccount(Vendor vendor) {
        // Idempotency — never create a second account for the same vendor
        if (vendor.getUserId() != null) {
            log.info("Vendor {} already has userId={}; skipping user creation.",
                    vendor.getVendorId(), vendor.getUserId());
            return;
        }

        // Derive a unique username from the vendor's email local-part
        String emailLocal   = vendor.getEmail().split("@")[0].toLowerCase().replaceAll("[^a-z0-9._]", "_");
        String baseUsername = "vendor_" + vendor.getVendorId() + "_" + emailLocal;
        String username     = ensureUsernameIsUnique(baseUsername);

        // Check email uniqueness in the users table
        if (userRepository.existsByEmail(vendor.getEmail())) {
            // This is a safety net — the vendor email was validated unique at registration time.
            // If we somehow reach here, log and continue with a null email on the user account
            // so we don't block vendor activation over a data inconsistency.
            log.error("Vendor {} email '{}' already exists in users table. " +
                            "Creating user account without email — manual cleanup required.",
                    vendor.getVendorId(), vendor.getEmail());
        }

        // Generate a temporary random password — the vendor must change it on first login
        String tempPassword = UUID.randomUUID().toString();
        log.warn("============================================================");
        log.warn("VENDOR TEMP PASSWORD for vendor {} ({}): {}",
                vendor.getVendorId(), vendor.getEmail(), tempPassword);
        log.warn("Share this with the vendor. They MUST change it via /auth/vendor/change-password.");
        log.warn("============================================================");

        User vendorUser = User.builder()
                .username(username)
                .password(passwordEncoder.encode(tempPassword))
                .name(vendor.getName())
                .role(Role.VENDOR)
                .email(userRepository.existsByEmail(vendor.getEmail()) ? null : vendor.getEmail())
                .phone(vendor.getPhone())
                .status(UserStatus.ACTIVE)
                .build();

        User saved = userRepository.save(vendorUser);

        // Store the userId back on the vendor — from this point, userId is the
        // operational identifier for all actions performed by this vendor.
        vendor.setUserId(saved.getUserId());
        vendorRepository.save(vendor);

        log.info("Vendor {} approved — User account created: userId={}, username={}, tempPassword={}" +
                        "Temporary password generated.",
                vendor.getVendorId(), saved.getUserId(), username,tempPassword);
    }

    /**
     * Ensures the generated username doesn't collide with an existing one by
     * appending a suffix when needed.
     */
    private String ensureUsernameIsUnique(String base) {
        if (!userRepository.existsByUsername(base)) {
            return base;
        }
        // Append incrementing suffix — practically never needed, but safe
        int suffix = 1;
        while (userRepository.existsByUsername(base + "_" + suffix)) {
            suffix++;
        }
        return base + "_" + suffix;
    }

    // ── Password change (verified vendors only) ───────────────────────────────

    /**
     * Allows an authenticated, verified (ACTIVE) vendor to change their password.
     * Requires the current (old) password for verification before updating.
     */
    @Override
    public void changeVendorPassword(String username, String oldPassword, String newPassword) {
        log.info("Vendor password change request for username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        // Only VENDOR role can use this endpoint
        if (user.getRole() != com.buildledger.enums.Role.VENDOR) {
            throw new BadRequestException("Only verified vendors can change their password via this endpoint.");
        }

        // Confirm the vendor record exists and is ACTIVE
        Vendor vendor = vendorRepository.findByUserId(user.getUserId())
                .orElseThrow(() -> new BadRequestException(
                        "No approved vendor account found for this user. Only verified vendors can change their password."));

        if (vendor.getStatus() != VendorStatus.ACTIVE) {
            throw new BadRequestException(
                    "Your vendor account is not ACTIVE (current status: " + vendor.getStatus() + "). " +
                            "Password change is only allowed for approved vendors.");
        }

        // Verify old password matches
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadRequestException("Current password is incorrect.");
        }

        if (newPassword == null || newPassword.isBlank() || newPassword.length() < 8) {
            throw new BadRequestException("New password must be at least 8 characters.");
        }

        if (oldPassword.equals(newPassword)) {
            throw new BadRequestException("New password must be different from the current password.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Vendor {} successfully changed their password.", username);
    }

    // ── Username update (verified vendors only) ───────────────────────────────

    /**
     * Allows an authenticated, verified (ACTIVE) vendor to update their own username.
     * Checks for uniqueness and throws a descriptive error if taken.
     */
    @Override
    public void updateVendorUsername(String currentUsername, String newUsername) {
        log.info("Vendor username update request: {} → {}", currentUsername, newUsername);

        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", currentUsername));

        if (user.getRole() != com.buildledger.enums.Role.VENDOR) {
            throw new BadRequestException("Only verified vendors can update their username via this endpoint.");
        }

        Vendor vendor = vendorRepository.findByUserId(user.getUserId())
                .orElseThrow(() -> new BadRequestException(
                        "No approved vendor account found for this user."));

        if (vendor.getStatus() != VendorStatus.ACTIVE) {
            throw new BadRequestException(
                    "Your vendor account is not ACTIVE (current status: " + vendor.getStatus() + "). " +
                            "Username update is only allowed for approved vendors.");
        }

        if (newUsername == null || newUsername.isBlank()) {
            throw new BadRequestException("Username cannot be blank.");
        }

        String trimmed = newUsername.trim().toLowerCase();

        if (trimmed.equals(currentUsername)) {
            throw new BadRequestException("New username must be different from your current username.");
        }

        if (userRepository.existsByUsername(trimmed)) {
            throw new BadRequestException("Username '" + trimmed + "' already exists. Please choose a different username.");
        }

        user.setUsername(trimmed);
        userRepository.save(user);
        log.info("Vendor username updated successfully: {} → {}", currentUsername, trimmed);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Vendor findVendorById(Long vendorId) {
        return vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", "id", vendorId));
    }

    private VendorDocument findDocumentById(Long documentId) {
        return vendorDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("VendorDocument", "id", documentId));
    }

    /**
     * Returns a human-readable list of transitions allowed from {@code status}
     * for inclusion in error messages.
     */
    private String allowedTransitions(VendorStatus status) {
        return switch (status) {
            case PENDING    -> "[ACTIVE, SUSPENDED]";
            case ACTIVE     -> "[SUSPENDED, BLACKLISTED]";
            case SUSPENDED  -> "[ACTIVE, BLACKLISTED]";

        };
    }

    private VendorResponseDTO mapToResponse(Vendor vendor) {
        return VendorResponseDTO.builder()
                .vendorId(vendor.getVendorId())
                .name(vendor.getName())
                .contactInfo(vendor.getContactInfo())
                .email(vendor.getEmail())
                .phone(vendor.getPhone())
                .category(vendor.getCategory())
                .address(vendor.getAddress())
                .status(vendor.getStatus())
                .userId(vendor.getUserId())   // expose userId so callers know when account exists
                .createdAt(vendor.getCreatedAt())
                .updatedAt(vendor.getUpdatedAt())
                .build();
    }

    private VendorDocumentResponseDTO mapDocumentToResponse(VendorDocument doc) {
        String fileUri      = doc.getFileUri();
        String displayName  = fileUri != null ? fileUri.replaceAll(".*[\\\\/]", "") : "unknown.pdf";

        return VendorDocumentResponseDTO.builder()
                .documentId(doc.getDocumentId())
                .vendorId(doc.getVendor().getVendorId())
                .vendorName(doc.getVendor().getName())
                .docType(doc.getDocType())
                .fileUri(displayName)
                .uploadedDate(doc.getUploadedDate())
                .verificationStatus(doc.getVerificationStatus())
                .remarks(doc.getRemarks())
                .reviewedBy(doc.getReviewedBy())
                .reviewedAt(doc.getReviewedAt())
                .reviewRemarks(doc.getReviewRemarks())
                .createdAt(doc.getCreatedAt())
                .build();
    }
}
