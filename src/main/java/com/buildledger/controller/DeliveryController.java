package com.buildledger.controller;

import com.buildledger.dto.request.DeliveryRequestDTO;
import com.buildledger.dto.response.ApiResponseDTO;
import com.buildledger.dto.response.DeliveryResponseDTO;
import com.buildledger.enums.DeliveryStatus;
import com.buildledger.service.DeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/deliveries")
@RequiredArgsConstructor
@Tag(name = "Delivery Tracking")
@SecurityRequirement(name = "bearerAuth")
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDOR')")
    @Operation(summary = "Record delivery [ADMIN, VENDOR]",
               description = " ADMIN, VENDOR can record deliveries.\n PROJECT_MANAGER, CLIENT cannot record deliveries.")
    public ResponseEntity<ApiResponseDTO<DeliveryResponseDTO>> createDelivery(@Valid @RequestBody DeliveryRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success("Delivery created successfully", deliveryService.createDelivery(request)));
    }

    @GetMapping
    @Operation(summary = "Get all deliveries [ALL roles]")
    public ResponseEntity<ApiResponseDTO<List<DeliveryResponseDTO>>> getAllDeliveries() {
        return ResponseEntity.ok(ApiResponseDTO.success("Deliveries retrieved", deliveryService.getAllDeliveries()));
    }

    @GetMapping("/{deliveryId}")
    @Operation(summary = "Get delivery by ID [ALL roles]")
    public ResponseEntity<ApiResponseDTO<DeliveryResponseDTO>> getDeliveryById(@PathVariable Long deliveryId) {
        return ResponseEntity.ok(ApiResponseDTO.success("Delivery retrieved", deliveryService.getDeliveryById(deliveryId)));
    }

    @GetMapping("/contract/{contractId}")
    @Operation(summary = "Get deliveries by contract [ALL roles]")
    public ResponseEntity<ApiResponseDTO<List<DeliveryResponseDTO>>> getDeliveriesByContract(@PathVariable Long contractId) {
        return ResponseEntity.ok(ApiResponseDTO.success("Deliveries retrieved", deliveryService.getDeliveriesByContract(contractId)));
    }

    @PutMapping("/{deliveryId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('VENDOR')")
    @Operation(summary = "Update delivery [ADMIN, PROJECT_MANAGER, VENDOR]",
               description = " ADMIN, PROJECT_MANAGER, VENDOR can update deliveries.\n CLIENT cannot update deliveries.")
    public ResponseEntity<ApiResponseDTO<DeliveryResponseDTO>> updateDelivery(
            @PathVariable Long deliveryId,
            @Valid @RequestBody DeliveryRequestDTO request) {
        return ResponseEntity.ok(ApiResponseDTO.success("Delivery updated", deliveryService.updateDelivery(deliveryId, request)));
    }

    @PatchMapping("/{deliveryId}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('VENDOR')")
    @Operation(summary = "Update delivery status",
               description = "VENDOR can mark as MARKED_DELIVERED.\nPROJECT_MANAGER and ADMIN can ACCEPT or REJECT deliveries.")
    public ResponseEntity<ApiResponseDTO<DeliveryResponseDTO>> updateDeliveryStatus(
            @PathVariable Long deliveryId,
            @RequestParam DeliveryStatus status) {
        return ResponseEntity.ok(ApiResponseDTO.success("Delivery status updated",
                deliveryService.updateDeliveryStatus(deliveryId, status)));
    }

    @DeleteMapping("/{deliveryId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Delete delivery [ADMIN, PROJECT_MANAGER]",
               description = " ADMIN, PROJECT_MANAGER\n VENDOR cannot delete deliveries.")
    public ResponseEntity<ApiResponseDTO<Void>> deleteDelivery(@PathVariable Long deliveryId) {
        deliveryService.deleteDelivery(deliveryId);
        return ResponseEntity.ok(ApiResponseDTO.success("Delivery deleted"));
    }
}
