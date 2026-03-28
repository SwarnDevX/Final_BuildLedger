package com.buildledger.service;

import com.buildledger.dto.request.ServiceRequestDTO;
import com.buildledger.dto.response.ServiceResponseDTO;
import com.buildledger.enums.ServiceStatus;
import java.util.List;

public interface ServiceTrackingService {
    ServiceResponseDTO createService(ServiceRequestDTO request);
    ServiceResponseDTO getServiceById(Long serviceId);
    List<ServiceResponseDTO> getAllServices();
    List<ServiceResponseDTO> getServicesByContract(Long contractId);
    ServiceResponseDTO updateServiceStatus(Long serviceId, ServiceStatus status);
    ServiceResponseDTO updateService(Long serviceId, ServiceRequestDTO request);
    void deleteService(Long serviceId);
}
