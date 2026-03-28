package com.buildledger.controller;

import com.buildledger.enums.PaymentStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
class PaymentControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void getAllPayments_ReturnsPaymentList() throws Exception {
        mockMvc.perform(get("/payments"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Payments retrieved"))
                .andExpect(jsonPath("$.data").isArray());
    }

//    @Test
//    void getPaymentById_ValidId_ReturnsPayment() throws Exception {
//        // Assuming payment with ID 1 exists in test data
//        mockMvc.perform(get("/payments/1"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType("application/json"))
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.message").value("Payment retrieved"))
//                .andExpect(jsonPath("$.data.id").value(1))
//                .andExpect(jsonPath("$.data.status").exists());
//    }
//
//    @Test
//    void getPaymentById_InvalidId_ReturnsNotFound() throws Exception {
//        mockMvc.perform(get("/payments/99999"))
//                .andExpect(status().isNotFound())
//                .andExpect(content().contentType("application/json"))
//                .andExpect(jsonPath("$.success").value(false))
//                .andExpect(jsonPath("$.message").exists());
//    }

    @Test
    void getPaymentsByInvoice_ValidInvoiceId_ReturnsPayments() throws Exception {
        // Assuming invoice with ID 1 exists and has payments
        mockMvc.perform(get("/payments/invoice/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Payments retrieved"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getPaymentsByInvoice_InvalidInvoiceId_ReturnsEmptyList() throws Exception {
        // Assuming invalid invoice ID returns empty list
        mockMvc.perform(get("/payments/invoice/99999"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Payments retrieved"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }
}