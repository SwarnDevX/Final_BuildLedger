package com.buildledger.controller;

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
class DeliveryControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void getAllDeliveries_ReturnsDeliveryList() throws Exception {
        mockMvc.perform(get("/deliveries"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Deliveries retrieved"))
                .andExpect(jsonPath("$.data").isArray());
    }

//    @Test
//    void getDeliveryById_ValidId_ReturnsDelivery() throws Exception {
//        // Assuming delivery with ID 1 exists in test data
//        mockMvc.perform(get("/deliveries/1"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType("application/json"))
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.message").value("Delivery retrieved"))
//                .andExpect(jsonPath("$.data.id").value(1))
//                .andExpect(jsonPath("$.data.status").exists());
//    }

//    @Test
//    void getDeliveryById_InvalidId_ReturnsNotFound() throws Exception {
//        mockMvc.perform(get("/deliveries/99999"))
//                .andExpect(status().isNotFound())
//                .andExpect(content().contentType("application/json"))
//                .andExpect(jsonPath("$.success").value(false))
//                .andExpect(jsonPath("$.message").exists());
//    }

    @Test
    void getDeliveriesByContract_ValidContractId_ReturnsDeliveries() throws Exception {
        // Assuming contract with ID 1 exists and has deliveries
        mockMvc.perform(get("/deliveries/contract/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Deliveries retrieved"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getDeliveriesByContract_InvalidContractId_ReturnsEmptyList() throws Exception {
        // Assuming invalid contract ID returns empty list
        mockMvc.perform(get("/deliveries/contract/99999"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Deliveries retrieved"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }
}