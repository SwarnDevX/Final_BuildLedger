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
class ServiceControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void getAllServices_ReturnsServiceList() throws Exception {
        mockMvc.perform(get("/services"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Services retrieved"))
                .andExpect(jsonPath("$.data").isArray());
    }

//    @Test
//    void getServiceById_ValidId_ReturnsService() throws Exception {
//        // Assuming service with ID 1 exists in test data
//        mockMvc.perform(get("/services/1"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType("application/json"))
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.message").value("Service retrieved"))
//                .andExpect(jsonPath("$.data.id").value(1))
//                .andExpect(jsonPath("$.data.status").exists());
//    }

//    @Test
//    void getServiceById_InvalidId_ReturnsNotFound() throws Exception {
//        mockMvc.perform(get("/services/99999"))
//                .andExpect(status().isNotFound())
//                .andExpect(content().contentType("application/json"))
//                .andExpect(jsonPath("$.success").value(false))
//                .andExpect(jsonPath("$.message").exists());
//    }

    @Test
    void getServicesByContract_ValidContractId_ReturnsServices() throws Exception {
        // Assuming contract with ID 1 exists and has services
        mockMvc.perform(get("/services/contract/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Services retrieved"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getServicesByContract_InvalidContractId_ReturnsEmptyList() throws Exception {
        // Assuming invalid contract ID returns empty list
        mockMvc.perform(get("/services/contract/99999"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Services retrieved"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }
}