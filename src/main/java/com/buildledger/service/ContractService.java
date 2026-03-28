package com.buildledger.service;

import com.buildledger.dto.request.ContractRequestDTO;
import com.buildledger.dto.request.ContractTermRequestDTO;
import com.buildledger.dto.response.ContractResponseDTO;
import com.buildledger.dto.response.ContractTermResponseDTO;
import com.buildledger.enums.ContractStatus;
import java.util.List;

public interface ContractService {
    ContractResponseDTO createContract(ContractRequestDTO request);
    ContractResponseDTO getContractById(Long contractId);
    List<ContractResponseDTO> getAllContracts();
    List<ContractResponseDTO> getContractsByVendor(Long vendorId);
    List<ContractResponseDTO> getContractsByProject(Long projectId);
    List<ContractResponseDTO> getContractsByStatus(ContractStatus status);
    ContractResponseDTO updateContractStatus(Long contractId, ContractStatus status);
    ContractResponseDTO updateContract(Long contractId, ContractRequestDTO request);
    void deleteContract(Long contractId);
    ContractTermResponseDTO addContractTerm(Long contractId, ContractTermRequestDTO request);
    List<ContractTermResponseDTO> getContractTerms(Long contractId);
    ContractTermResponseDTO editContractTerm(Long termId, ContractTermRequestDTO request);
    void deleteContractTerm(Long termId);
}
