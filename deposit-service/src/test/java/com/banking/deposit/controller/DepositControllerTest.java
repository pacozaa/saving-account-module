package com.banking.deposit.controller;

import com.banking.deposit.dto.DepositRequest;
import com.banking.deposit.dto.DepositResponse;
import com.banking.deposit.service.DepositService;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for DepositController
 * Uses @WebMvcTest for lightweight controller testing
 */
@WebMvcTest(DepositController.class)
class DepositControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DepositService depositService;

    private DepositRequest validRequest;
    private DepositResponse successResponse;

    @BeforeEach
    void setUp() {
        validRequest = new DepositRequest(
                "1234567",
                new BigDecimal("1000.00"),
                5L,
                "Cash deposit"
        );

        successResponse = new DepositResponse(
                101L,
                "1234567",
                new BigDecimal("1000.00"),
                new BigDecimal("2500.00"),
                "Deposit successful"
        );
    }

    @Test
    void testDeposit_ValidRequest_WithTellerRole_Returns200() throws Exception {
        // Given
        when(depositService.processDeposit(any(DepositRequest.class)))
                .thenReturn(successResponse);

        // When & Then
        mockMvc.perform(post("/deposit")
                        .header("X-User-Role", "TELLER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(101))
                .andExpect(jsonPath("$.accountId").value("1234567"))
                .andExpect(jsonPath("$.amount").value(1000.00))
                .andExpect(jsonPath("$.newBalance").value(2500.00))
                .andExpect(jsonPath("$.message").value("Deposit successful"));
    }

    @Test
    void testDeposit_WithCustomerRole_Returns403() throws Exception {
        // When & Then
        mockMvc.perform(post("/deposit")
                        .header("X-User-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Only tellers are authorized to perform deposits"));
    }

    @Test
    void testDeposit_WithPersonRole_Returns403() throws Exception {
        // When & Then
        mockMvc.perform(post("/deposit")
                        .header("X-User-Role", "PERSON")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Only tellers are authorized to perform deposits"));
    }

    @Test
    void testDeposit_WithoutRole_Returns403() throws Exception {
        // When & Then - No X-User-Role header
        mockMvc.perform(post("/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Only tellers are authorized to perform deposits"));
    }

    @Test
    void testDeposit_InvalidRequest_MissingAccountId_Returns400() throws Exception {
        // Given
        DepositRequest invalidRequest = new DepositRequest(
                null, // Missing account ID
                new BigDecimal("1000.00"),
                5L,
                "Cash deposit"
        );

        // When & Then
        mockMvc.perform(post("/deposit")
                        .header("X-User-Role", "TELLER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeposit_InvalidRequest_MissingAmount_Returns400() throws Exception {
        // Given
        DepositRequest invalidRequest = new DepositRequest(
                "1234567",
                null, // Missing amount
                5L,
                "Cash deposit"
        );

        // When & Then
        mockMvc.perform(post("/deposit")
                        .header("X-User-Role", "TELLER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeposit_InvalidRequest_NegativeAmount_Returns400() throws Exception {
        // Given
        DepositRequest invalidRequest = new DepositRequest(
                "1234567",
                new BigDecimal("-100.00"), // Negative amount
                5L,
                "Cash deposit"
        );

        // When & Then
        mockMvc.perform(post("/deposit")
                        .header("X-User-Role", "TELLER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeposit_InvalidRequest_ZeroAmount_Returns400() throws Exception {
        // Given
        DepositRequest invalidRequest = new DepositRequest(
                "1234567",
                BigDecimal.ZERO, // Zero amount
                5L,
                "Cash deposit"
        );

        // When & Then
        mockMvc.perform(post("/deposit")
                        .header("X-User-Role", "TELLER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeposit_InvalidRequest_AmountLessThanOne_Returns400() throws Exception {
        // Given
        DepositRequest invalidRequest = new DepositRequest(
                "1234567",
                new BigDecimal("0.5"), // Amount less than 1
                5L,
                "Cash deposit"
        );

        // When & Then
        mockMvc.perform(post("/deposit")
                        .header("X-User-Role", "TELLER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeposit_AccountNotFound_Returns404() throws Exception {
        // Given
        Request request = Request.create(
                Request.HttpMethod.GET,
                "/accounts/9999999",
                new HashMap<>(),
                null,
                new RequestTemplate()
        );

        when(depositService.processDeposit(any(DepositRequest.class)))
                .thenThrow(new FeignException.NotFound(
                        "Account not found",
                        request,
                        null,
                        null
                ));

        // When & Then
        mockMvc.perform(post("/deposit")
                        .header("X-User-Role", "TELLER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeposit_ServiceError_Returns500() throws Exception {
        // Given
        Request request = Request.create(
                Request.HttpMethod.PUT,
                "/accounts/1234567/balance",
                new HashMap<>(),
                null,
                new RequestTemplate()
        );

        when(depositService.processDeposit(any(DepositRequest.class)))
                .thenThrow(new FeignException.InternalServerError(
                        "Service error",
                        request,
                        null,
                        null
                ));

        // When & Then
        mockMvc.perform(post("/deposit")
                        .header("X-User-Role", "TELLER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testDeposit_WithoutOptionalFields_Returns200() throws Exception {
        // Given
        DepositRequest minimalRequest = new DepositRequest(
                "1234567",
                new BigDecimal("500.00"),
                null, // No teller ID
                null  // No description
        );

        DepositResponse minimalResponse = new DepositResponse(
                102L,
                "1234567",
                new BigDecimal("500.00"),
                new BigDecimal("2000.00"),
                "Deposit successful"
        );

        when(depositService.processDeposit(any(DepositRequest.class)))
                .thenReturn(minimalResponse);

        // When & Then
        mockMvc.perform(post("/deposit")
                        .header("X-User-Role", "TELLER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(minimalRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(102))
                .andExpect(jsonPath("$.accountId").value("1234567"))
                .andExpect(jsonPath("$.amount").value(500.00))
                .andExpect(jsonPath("$.newBalance").value(2000.00));
    }

    @Test
    void testHealth_Returns200() throws Exception {
        // When & Then
        mockMvc.perform(get("/deposit/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Deposit Service is running"));
    }

    @Test
    void testDeposit_InvalidJson_Returns500() throws Exception {
        // Given - malformed JSON (Spring handles this as 500 by default in GlobalExceptionHandler)
        String invalidJson = "{\"accountId\": \"1234567\", \"amount\": invalid}";

        // When & Then
        mockMvc.perform(post("/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isInternalServerError());
    }
}
