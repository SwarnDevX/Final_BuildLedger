package com.buildledger.service;

import com.buildledger.dto.request.InvoiceRequestDTO;
import com.buildledger.dto.response.InvoiceResponseDTO;
import com.buildledger.enums.InvoiceStatus;
import java.util.List;

public interface InvoiceService {
    InvoiceResponseDTO submitInvoice(InvoiceRequestDTO request);
    InvoiceResponseDTO getInvoiceById(Long invoiceId);
    List<InvoiceResponseDTO> getAllInvoices();
    List<InvoiceResponseDTO> getInvoicesByContract(Long contractId);
    List<InvoiceResponseDTO> getInvoicesByStatus(InvoiceStatus status);
    InvoiceResponseDTO approveInvoice(Long invoiceId);
    InvoiceResponseDTO rejectInvoice(Long invoiceId, String reason);
    void deleteInvoice(Long invoiceId);
}
