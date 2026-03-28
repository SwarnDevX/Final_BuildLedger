package com.buildledger.controller;

import com.buildledger.dto.request.CreateVendorRequestDTO;
import com.buildledger.dto.request.UpdateVendorRequestDTO;
import com.buildledger.dto.response.ApiResponseDTO;
import com.buildledger.dto.response.VendorDocumentResponseDTO;
import com.buildledger.dto.response.VendorResponseDTO;
import com.buildledger.enums.DocumentType;
import com.buildledger.enums.VendorStatus;
import com.buildledger.enums.VerificationStatus;
import com.buildledger.service.VendorService;
import com.buildledger.service.impl.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/vendors")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Vendor Management")
@SecurityRequirement(name = "bearerAuth")
public class VendorController {

    private final VendorService vendorService;
    private final AuditLogService auditLogService;

    // ── Vendor Self-Registration (PUBLIC) ────────────────────────────────────

    @PostMapping("/register")
    @Operation(
            summary = "Vendor self-registration [PUBLIC — no auth required]",
            description = """
            Vendors register themselves. No authentication needed.
            The vendor starts in **PENDING** status.
            Admin/PM must upload and approve documents before the vendor gets a user account.
            A temporary password is printed in the server console on approval.
            """
    )
    public ResponseEntity<ApiResponseDTO<VendorResponseDTO>> registerVendor(
            @Valid @RequestBody CreateVendorRequestDTO request) {
        VendorResponseDTO response = vendorService.registerVendor(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(
                        "Vendor registered successfully. Please wait for document verification.", response));
    }

    // ── Vendor CRUD (Admin) ──────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new vendor [ADMIN only]")
    public ResponseEntity<ApiResponseDTO<VendorResponseDTO>> createVendor(
            @Valid @RequestBody CreateVendorRequestDTO request,
            Authentication auth, HttpServletRequest httpRequest) {
        VendorResponseDTO response = vendorService.createVendor(request);
        auditLogService.logAction(null, auth.getName(), "CREATE", "VENDOR",
                "Created vendor: " + response.getName(), httpRequest.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success("Vendor created successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all vendors [ALL roles]")
    public ResponseEntity<ApiResponseDTO<List<VendorResponseDTO>>> getAllVendors() {
        return ResponseEntity.ok(ApiResponseDTO.success("Vendors retrieved", vendorService.getAllVendors()));
    }

    @GetMapping("/{vendorId}")
    @Operation(summary = "Get vendor by ID [ALL roles]")
    public ResponseEntity<ApiResponseDTO<VendorResponseDTO>> getVendorById(@PathVariable Long vendorId) {
        return ResponseEntity.ok(ApiResponseDTO.success("Vendor retrieved", vendorService.getVendorById(vendorId)));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get vendors by status [ALL roles]")
    public ResponseEntity<ApiResponseDTO<List<VendorResponseDTO>>> getVendorsByStatus(@PathVariable VendorStatus status) {
        return ResponseEntity.ok(ApiResponseDTO.success("Vendors retrieved", vendorService.getVendorsByStatus(status)));
    }

    @PutMapping("/{vendorId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update vendor details [ADMIN only]")
    public ResponseEntity<ApiResponseDTO<VendorResponseDTO>> updateVendor(
            @PathVariable Long vendorId,
            @Valid @RequestBody UpdateVendorRequestDTO request,
            Authentication auth, HttpServletRequest httpRequest) {
        VendorResponseDTO response = vendorService.updateVendor(vendorId, request);
        auditLogService.logAction(null, auth.getName(), "UPDATE", "VENDOR",
                "Updated vendor id: " + vendorId, httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponseDTO.success("Vendor updated successfully", response));
    }

    @DeleteMapping("/{vendorId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a vendor [ADMIN only]",
            description = "Also deletes all associated PDF documents from disk.")
    public ResponseEntity<ApiResponseDTO<Void>> deleteVendor(
            @PathVariable Long vendorId,
            Authentication auth, HttpServletRequest httpRequest) {
        vendorService.deleteVendor(vendorId);
        auditLogService.logAction(null, auth.getName(), "DELETE", "VENDOR",
                "Deleted vendor id: " + vendorId, httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponseDTO.success("Vendor deleted successfully"));
    }

    // ── Document Upload ──────────────────────────────────────────────────────

    @PostMapping(value = "/{vendorId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload a compliance document PDF [PUBLIC]",
            description = """
        Upload a PDF document for a vendor. File is stored in:
        `<project-root>/uploads/vendor_{id}/uuid_filename.pdf`

        **Rules:**
        - File must be `.pdf` with content-type `application/pdf`
        - Max size: 10 MB
        - Status starts as **PENDING**

        **Auto-status logic:**
        After PM reviews all documents:
        - All APPROVED → vendor status becomes **ACTIVE**
        - Any REJECTED → vendor status becomes **SUSPENDED**
        """
    )
    public ResponseEntity<ApiResponseDTO<VendorDocumentResponseDTO>> uploadDocument(
            @PathVariable Long vendorId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("docType") DocumentType docType,
            @RequestParam(value = "remarks", required = false) String remarks,
            HttpServletRequest httpRequest) {

        // No auth → use system identity
        String username = "PUBLIC_USER";

        VendorDocumentResponseDTO response = vendorService.uploadDocument(
                vendorId, file, docType, remarks, username);

        auditLogService.logAction(
                null,
                username,
                "UPLOAD_DOCUMENT",
                "VENDOR_DOCUMENT",
                "Uploaded: " + file.getOriginalFilename() + " [" + docType + "] vendorId=" + vendorId,
                httpRequest.getRemoteAddr()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(
                        "Document uploaded successfully. Awaiting review.",
                        response
                ));
    }

    // ── Document Listing ─────────────────────────────────────────────────────

    @GetMapping("/{vendorId}/documents")
    @Operation(summary = "Get all documents for a vendor [ALL roles]")
    public ResponseEntity<ApiResponseDTO<List<VendorDocumentResponseDTO>>> getVendorDocuments(
            @PathVariable Long vendorId) {
        return ResponseEntity.ok(ApiResponseDTO.success("Documents retrieved",
                vendorService.getVendorDocuments(vendorId)));
    }

    @GetMapping("/documents/pending-review")
    @PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('ADMIN')")
    @Operation(
            summary = "Get all documents pending review [PROJECT_MANAGER / ADMIN]",
            description = "Returns all PENDING documents across all vendors — the PM review queue."
    )
    public ResponseEntity<ApiResponseDTO<List<VendorDocumentResponseDTO>>> getPendingDocuments() {
        return ResponseEntity.ok(ApiResponseDTO.success("Pending documents retrieved",
                vendorService.getDocumentsByStatus(VerificationStatus.PENDING)));
    }

    @GetMapping("/documents/status/{status}")
    @PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('ADMIN')")
    @Operation(
            summary = "Get documents by verification status [PROJECT_MANAGER / ADMIN]",
            description = "Filter by: `PENDING`, `APPROVED`, `REJECTED`"
    )
    public ResponseEntity<ApiResponseDTO<List<VendorDocumentResponseDTO>>> getDocumentsByStatus(
            @PathVariable VerificationStatus status) {
        return ResponseEntity.ok(ApiResponseDTO.success("Documents retrieved",
                vendorService.getDocumentsByStatus(status)));
    }

    // ── Document Download ─────────────────────────────────────────────────────

    @GetMapping(value = "/documents/{documentId}/download", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('ADMIN') or hasRole('VENDOR') or hasRole('COMPLIANCE_OFFICER')")
    @Operation(
            summary = "Download vendor document PDF [PM / ADMIN / VENDOR / COMPLIANCE_OFFICER]",
            description = """
            Streams the stored PDF file.

            **In Swagger UI:** Click Execute → then click the **Download file** link in the response.
            The PDF will open in your browser or download depending on your browser settings.
            """,
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "PDF file",
                            content = @Content(mediaType = MediaType.APPLICATION_PDF_VALUE,
                                    schema = @Schema(type = "string", format = "binary"))
                    )
            }
    )
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long documentId,
            Authentication auth,
            HttpServletRequest httpRequest) {

        Resource resource = vendorService.downloadDocument(documentId);

        auditLogService.logAction(null, auth.getName(), "DOWNLOAD_DOCUMENT", "VENDOR_DOCUMENT",
                "Downloaded document id: " + documentId, httpRequest.getRemoteAddr());

        // Extract original filename from the stored path (everything after last / or \)
        String filename = resource.getFilename() != null
                ? resource.getFilename().replaceAll(".*[\\\\/]", "")
                : "document.pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    // ── Document Review ───────────────────────────────────────────────────────

    @PutMapping("/documents/{documentId}/verify")
    @PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('ADMIN')")
    @Operation(
            summary = "Review vendor document — APPROVED or REJECTED [PROJECT_MANAGER / ADMIN]",
            description = """
            ✅ PROJECT_MANAGER and ADMIN can approve or reject a vendor document.
            ❌ VENDOR cannot review their own documents.

            **Allowed status values:** `APPROVED` or `REJECTED`

            **Business rules (matching reference project):**
            - APPROVED → REJECTED: ❌ blocked (once approved, cannot be rejected)
            - REJECTED → APPROVED: ❌ blocked (once rejected, cannot be approved)
            - Same status again: ❌ blocked

            **Auto vendor status update (triggered after every review):**
            - All documents APPROVED → vendor status → **ACTIVE**
            - Any document REJECTED  → vendor status → **SUSPENDED**
            """
    )
    public ResponseEntity<ApiResponseDTO<VendorDocumentResponseDTO>> reviewDocument(
            @PathVariable Long documentId,
            @RequestParam VerificationStatus status,
            @RequestParam(required = false) String reviewRemarks,
            Authentication auth,
            HttpServletRequest httpRequest) {

        VendorDocumentResponseDTO response = vendorService.reviewDocument(
                documentId, status, reviewRemarks, auth.getName());

        auditLogService.logAction(null, auth.getName(), "REVIEW_DOCUMENT", "VENDOR_DOCUMENT",
                "Document id=" + documentId + " → " + status
                        + (reviewRemarks != null ? " | " + reviewRemarks : ""),
                httpRequest.getRemoteAddr());

        String message = status == VerificationStatus.APPROVED
                ? "Document approved successfully."
                : "Document rejected.";

        return ResponseEntity.ok(ApiResponseDTO.success(message, response));
    }
}
