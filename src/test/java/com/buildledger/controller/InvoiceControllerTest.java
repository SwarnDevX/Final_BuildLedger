package com.buildledger.controller;

import com.buildledger.enums.InvoiceStatus;
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
class InvoiceControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void getAllInvoices_ReturnsInvoiceList() throws Exception {
        mockMvc.perform(get("/invoices"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Invoices retrieved"))
                .andExpect(jsonPath("$.data").isArray());
    }

//    @Test
//    void getInvoiceById_ValidId_ReturnsInvoice() throws Exception {
//        // Assuming invoice with ID 1 exists in test data
//        mockMvc.perform(get("/invoices/1"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType("application/json"))
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.message").value("Invoice retrieved"))
//                .andExpect(jsonPath("$.data.id").value(1))
//                .andExpect(jsonPath("$.data.status").exists());
//    }

//    @Test
//    void getInvoiceById_InvalidId_ReturnsNotFound() throws Exception {
//        mockMvc.perform(get("/invoices/99999"))
//                .andExpect(status().isNotFound())
//                .andExpect(content().contentType("application/json"))
//                .andExpect(jsonPath("$.success").value(false))
//                .andExpect(jsonPath("$.message").exists());
//    }

    @Test
    void getInvoicesByContract_ValidContractId_ReturnsInvoices() throws Exception {
        // Assuming contract with ID 1 exists and has invoices
        mockMvc.perform(get("/invoices/contract/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Invoices retrieved"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getInvoicesByContract_InvalidContractId_ReturnsEmptyList() throws Exception {
        // Assuming invalid contract ID returns empty list
        mockMvc.perform(get("/invoices/contract/99999"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Invoices retrieved"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void getInvoicesByStatus_ValidStatus_ReturnsInvoices() throws Exception {
        mockMvc.perform(get("/invoices/status/UNDER_REVIEW"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Invoices retrieved"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getInvoicesByStatus_ApprovedStatus_ReturnsInvoices() throws Exception {
        mockMvc.perform(get("/invoices/status/APPROVED"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Invoices retrieved"))
                .andExpect(jsonPath("$.data").isArray());
    }
}