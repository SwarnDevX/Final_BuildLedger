package com.buildledger.service;

import com.buildledger.dto.request.CreateVendorRequestDTO;
import com.buildledger.dto.request.UpdateVendorRequestDTO;
import com.buildledger.dto.response.VendorDocumentResponseDTO;
import com.buildledger.dto.response.VendorResponseDTO;
import com.buildledger.enums.DocumentType;
import com.buildledger.enums.VendorStatus;
import com.buildledger.enums.VerificationStatus;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VendorService {

    // ── Vendor CRUD ──────────────────────────────────────────────────────────

    /** PUBLIC self-registration — vendor submits their own details, starts PENDING */
    VendorResponseDTO registerVendor(CreateVendorRequestDTO request);

    VendorResponseDTO createVendor(CreateVendorRequestDTO request);
    VendorResponseDTO getVendorById(Long vendorId);
    List<VendorResponseDTO> getAllVendors();
    List<VendorResponseDTO> getVendorsByStatus(VendorStatus status);
    VendorResponseDTO updateVendor(Long vendorId, UpdateVendorRequestDTO request);
    void deleteVendor(Long vendorId);

    // ── Document lifecycle ───────────────────────────────────────────────────

    /** VENDOR uploads a PDF — validates, stores, returns PENDING document record */
    VendorDocumentResponseDTO uploadDocument(Long vendorId, MultipartFile file,
                                             DocumentType docType, String remarks,
                                             String uploaderUsername);

    /** All documents for a vendor */
    List<VendorDocumentResponseDTO> getVendorDocuments(Long vendorId);

    /** All documents filtered by verification status */
    List<VendorDocumentResponseDTO> getDocumentsByStatus(VerificationStatus status);

    /** Stream the PDF from disk */
    Resource downloadDocument(Long documentId);

    /**
     * PROJECT_MANAGER / ADMIN reviews a document (APPROVED or REJECTED).
     * After every review, triggers autoUpdateVendorStatus.
     */
    VendorDocumentResponseDTO reviewDocument(Long documentId, VerificationStatus status,
                                             String reviewRemarks, String reviewerUsername);

    /**
     * Automatically updates the parent vendor's status based on its documents:
     *  - All docs APPROVED  → vendor becomes ACTIVE
     *  - Any doc REJECTED   → vendor becomes SUSPENDED (userId stays null)
     *  - Otherwise          → vendor stays PENDING
     * Only acts when vendor is currently PENDING.
     */
    void autoUpdateVendorStatus(Long vendorId);

    /**
     * Allows a verified (ACTIVE) vendor to set their own password.
     * Requires the current (old) password for verification before updating.
     * @param username    the authenticated vendor's username
     * @param oldPassword the vendor's current password (for verification)
     * @param newPassword the new password to set
     */
    void changeVendorPassword(String username, String oldPassword, String newPassword);

    /**
     * Allows a verified (ACTIVE) vendor to update their own username.
     * @param currentUsername  the authenticated vendor's current username
     * @param newUsername      the desired new username
     */
    void updateVendorUsername(String currentUsername, String newUsername);
}
