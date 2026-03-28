package com.buildledger.controller;

import com.buildledger.dto.request.ComplianceRecordRequestDTO;
import com.buildledger.dto.response.ApiResponseDTO;
import com.buildledger.dto.response.ComplianceRecordResponseDTO;
import com.buildledger.enums.ComplianceType;
import com.buildledger.service.ComplianceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ComplianceControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ComplianceService complianceService;

    @InjectMocks
    private ComplianceController complianceController;

    private ObjectMapper objectMapper;

    private ComplianceRecordResponseDTO mockResponseDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(complianceController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // for LocalDate

        mockResponseDTO = ComplianceRecordResponseDTO.builder()
                .complianceId(1L)
                .contractId(1L)
                .type(ComplianceType.DELIVERY)
                .result("Pass")
                .date(LocalDate.now())
                .notes("All good")
                .build();
    }

    @Test
    void createComplianceRecord_ReturnsCreated() throws Exception {
        ComplianceRecordRequestDTO requestDTO = new ComplianceRecordRequestDTO();
        requestDTO.setContractId(1L);
        requestDTO.setType(ComplianceType.DELIVERY);
        requestDTO.setResult("Pass");
        requestDTO.setDate(LocalDate.now().plusDays(1)); // valid FutureOrPresent
        requestDTO.setNotes("All good");

        when(complianceService.createComplianceRecord(any(ComplianceRecordRequestDTO.class))).thenReturn(mockResponseDTO);

        mockMvc.perform(post("/compliance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Compliance record created"))
                .andExpect(jsonPath("$.data.complianceId").value(1));
    }

    @Test
    void getAllComplianceRecords_ReturnsComplianceRecordList() throws Exception {
        List<ComplianceRecordResponseDTO> mockList = Arrays.asList(mockResponseDTO);

        when(complianceService.getAllComplianceRecords()).thenReturn(mockList);

        mockMvc.perform(get("/compliance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Compliance records retrieved"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].complianceId").value(1));
    }

    @Test
    void getComplianceRecordById_ValidId_ReturnsComplianceRecord() throws Exception {
        when(complianceService.getComplianceRecordById(1L)).thenReturn(mockResponseDTO);

        mockMvc.perform(get("/compliance/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Compliance record retrieved"))
                .andExpect(jsonPath("$.data.complianceId").value(1));
    }

    @Test
    void getComplianceRecordsByContract_ValidContractId_ReturnsComplianceRecords() throws Exception {
        List<ComplianceRecordResponseDTO> mockList = Arrays.asList(mockResponseDTO);

        when(complianceService.getComplianceRecordsByContract(1L)).thenReturn(mockList);

        mockMvc.perform(get("/compliance/contract/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Compliance records retrieved"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].complianceId").value(1));
    }
}