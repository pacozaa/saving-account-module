package com.banking.transfer.controller;

import com.banking.transfer.dto.TransferRequest;
import com.banking.transfer.dto.TransferResponse;
import com.banking.transfer.exception.AccountNotFoundException;
import com.banking.transfer.exception.InsufficientFundsException;
import com.banking.transfer.exception.SameAccountTransferException;
import com.banking.transfer.service.TransferService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for TransferController
 */
@WebMvcTest(TransferController.class)
class TransferControllerTest {

    private static final Long AUTHENTICATED_USER_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransferService transferService;

    private TransferRequest validRequest;
    private TransferResponse transferResponse;

    @BeforeEach
    void setUp() {
        validRequest = new TransferRequest(
                101L,
                102L,
                new BigDecimal("500.00"),
                "Payment for services"
        );

        transferResponse = new TransferResponse(
                1001L,
                101L,
                102L,
                new BigDecimal("500.00"),
                new BigDecimal("1000.00"),
                new BigDecimal("1500.00"),
                "Transfer successful"
        );
    }

    @Test
    void testTransfer_ValidRequest_Returns200() throws Exception {
        // Given
        when(transferService.transfer(any(TransferRequest.class), any(Long.class))).thenReturn(transferResponse);

        // When & Then
        mockMvc.perform(post("/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", AUTHENTICATED_USER_ID)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.transactionId", is(1001)))
                .andExpect(jsonPath("$.fromAccountId", is(101)))
                .andExpect(jsonPath("$.toAccountId", is(102)))
                .andExpect(jsonPath("$.amount", is(500.00)))
                .andExpect(jsonPath("$.fromAccountNewBalance", is(1000.00)))
                .andExpect(jsonPath("$.toAccountNewBalance", is(1500.00)))
                .andExpect(jsonPath("$.message", is("Transfer successful")));

        verify(transferService).transfer(any(TransferRequest.class), any(Long.class));
    }

    @Test
    void testTransfer_InvalidRequest_MissingFromAccountId_Returns400() throws Exception {
        // Given
        TransferRequest invalidRequest = new TransferRequest(
                null, // missing from account ID
                102L,
                new BigDecimal("500.00"),
                "Payment"
        );

        // When & Then
        mockMvc.perform(post("/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", AUTHENTICATED_USER_ID)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(transferService, never()).transfer(any(), any());
    }

    @Test
    void testTransfer_InvalidRequest_MissingToAccountId_Returns400() throws Exception {
        // Given
        TransferRequest invalidRequest = new TransferRequest(
                101L,
                null, // missing to account ID
                new BigDecimal("500.00"),
                "Payment"
        );

        // When & Then
        mockMvc.perform(post("/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", AUTHENTICATED_USER_ID)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(transferService, never()).transfer(any(), any());
    }

    @Test
    void testTransfer_InvalidRequest_MissingAmount_Returns400() throws Exception {
        // Given
        TransferRequest invalidRequest = new TransferRequest(
                101L,
                102L,
                null, // missing amount
                "Payment"
        );

        // When & Then
        mockMvc.perform(post("/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", AUTHENTICATED_USER_ID)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(transferService, never()).transfer(any(), any());
    }

    @Test
    void testTransfer_InvalidRequest_NegativeAmount_Returns400() throws Exception {
        // Given
        TransferRequest invalidRequest = new TransferRequest(
                101L,
                102L,
                new BigDecimal("-100.00"), // negative amount
                "Payment"
        );

        // When & Then
        mockMvc.perform(post("/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", AUTHENTICATED_USER_ID)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(transferService, never()).transfer(any(), any());
    }

    @Test
    void testTransfer_InvalidRequest_ZeroAmount_Returns400() throws Exception {
        // Given
        TransferRequest invalidRequest = new TransferRequest(
                101L,
                102L,
                new BigDecimal("0.00"), // zero amount
                "Payment"
        );

        // When & Then
        mockMvc.perform(post("/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", AUTHENTICATED_USER_ID)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(transferService, never()).transfer(any(), any());
    }

    @Test
    void testTransfer_SameAccountTransfer_Returns400() throws Exception {
        // Given
        when(transferService.transfer(any(TransferRequest.class), any(Long.class)))
                .thenThrow(new SameAccountTransferException());

        // When & Then
        mockMvc.perform(post("/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", AUTHENTICATED_USER_ID)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());

        verify(transferService).transfer(any(TransferRequest.class), any(Long.class));
    }

    @Test
    void testTransfer_InsufficientFunds_Returns400() throws Exception {
        // Given
        when(transferService.transfer(any(TransferRequest.class), any(Long.class)))
                .thenThrow(new InsufficientFundsException("101"));

        // When & Then
        mockMvc.perform(post("/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", AUTHENTICATED_USER_ID)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());

        verify(transferService).transfer(any(TransferRequest.class), any(Long.class));
    }

    @Test
    void testTransfer_AccountNotFound_Returns404() throws Exception {
        // Given
        when(transferService.transfer(any(TransferRequest.class), any(Long.class)))
                .thenThrow(new AccountNotFoundException("102"));

        // When & Then
        mockMvc.perform(post("/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", AUTHENTICATED_USER_ID)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound());

        verify(transferService).transfer(any(TransferRequest.class), any(Long.class));
    }

    @Test
    void testHealth_ReturnsHealthyStatus() throws Exception {
        // When & Then
        mockMvc.perform(get("/transfer/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Transfer Service is running"));
    }
}
