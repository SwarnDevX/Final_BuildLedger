package com.buildledger.service;

import com.buildledger.dto.request.DeliveryRequestDTO;
import com.buildledger.dto.response.DeliveryResponseDTO;
import com.buildledger.enums.DeliveryStatus;
import java.util.List;

public interface DeliveryService {
    DeliveryResponseDTO createDelivery(DeliveryRequestDTO request);
    DeliveryResponseDTO getDeliveryById(Long deliveryId);
    List<DeliveryResponseDTO> getAllDeliveries();
    List<DeliveryResponseDTO> getDeliveriesByContract(Long contractId);
    DeliveryResponseDTO updateDeliveryStatus(Long deliveryId, DeliveryStatus status);
    DeliveryResponseDTO updateDelivery(Long deliveryId, DeliveryRequestDTO request);
    void deleteDelivery(Long deliveryId);
}
