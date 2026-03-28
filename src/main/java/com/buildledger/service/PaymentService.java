package com.buildledger.service;

import com.buildledger.dto.request.PaymentRequestDTO;
//import com.buildledger.dto.request.PaymentRequest;
import com.buildledger.dto.response.PaymentResponseDTO;
//import com.buildledger.dto.response.PaymentResponse;
import com.buildledger.enums.PaymentStatus;
import java.util.List;

public interface PaymentService {
    PaymentResponseDTO processPayment(PaymentRequestDTO request);
    PaymentResponseDTO getPaymentById(Long paymentId);
    List<PaymentResponseDTO> getAllPayments();
    List<PaymentResponseDTO> getPaymentsByInvoice(Long invoiceId);
    PaymentResponseDTO updatePaymentStatus(Long paymentId, PaymentStatus status);
}
