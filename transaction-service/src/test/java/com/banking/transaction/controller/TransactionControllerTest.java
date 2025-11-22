package com.banking.transaction.controller;

import com.banking.transaction.dto.LogTransactionRequest;
import com.banking.transaction.dto.TransactionDto;
import com.banking.transaction.exception.TransactionNotFoundException;
import com.banking.transaction.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@DisplayName("Transaction Controller Unit Tests")
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    private TransactionDto transactionDto;
    private LogTransactionRequest logRequest;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        transactionDto = new TransactionDto();
        transactionDto.setId(1001L);
        transactionDto.setAccountId(101L);
        transactionDto.setType("DEPOSIT");
        transactionDto.setAmount(new BigDecimal("500.00"));
        transactionDto.setRelatedAccountId(null);
        transactionDto.setDescription("Test deposit");
        transactionDto.setTimestamp(now);
        transactionDto.setStatus("COMPLETED");

        logRequest = new LogTransactionRequest();
        logRequest.setAccountId(101L);
        logRequest.setTransactionType("DEPOSIT");
        logRequest.setAmount(new BigDecimal("500.00"));
        logRequest.setDescription("Test deposit");
    }

    @Test
    @DisplayName("Should log transaction successfully and return 201")
    void testLogTransaction_ValidRequest_Returns201() throws Exception {
        // Given
        when(transactionService.logTransaction(any(LogTransactionRequest.class))).thenReturn(transactionDto);

        // When & Then
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1001L))
                .andExpect(jsonPath("$.accountId").value(101L))
                .andExpect(jsonPath("$.type").value("DEPOSIT"))
                .andExpect(jsonPath("$.amount").value(500.00))
                .andExpect(jsonPath("$.description").value("Test deposit"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        verify(transactionService, times(1)).logTransaction(any(LogTransactionRequest.class));
    }

    @Test
    @DisplayName("Should return 400 when logging transaction with invalid request (null accountId)")
    void testLogTransaction_InvalidRequest_Returns400() throws Exception {
        // Given
        logRequest.setAccountId(null); // Invalid

        // When & Then
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logRequest)))
                .andExpect(status().isBadRequest());

        verify(transactionService, never()).logTransaction(any());
    }

    @Test
    @DisplayName("Should return 400 when logging transaction with negative amount")
    void testLogTransaction_NegativeAmount_Returns400() throws Exception {
        // Given
        logRequest.setAmount(new BigDecimal("-100.00")); // Invalid

        // When & Then
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logRequest)))
                .andExpect(status().isBadRequest());

        verify(transactionService, never()).logTransaction(any());
    }

    @Test
    @DisplayName("Should return 400 when logging transaction with null transaction type")
    void testLogTransaction_NullTransactionType_Returns400() throws Exception {
        // Given
        logRequest.setTransactionType(null); // Invalid

        // When & Then
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logRequest)))
                .andExpect(status().isBadRequest());

        verify(transactionService, never()).logTransaction(any());
    }

    @Test
    @DisplayName("Should get transaction by ID successfully and return 200")
    void testGetTransaction_ValidId_Returns200() throws Exception {
        // Given
        Long transactionId = 1001L;
        Long userId = 1L;
        String role = "PERSON";
        when(transactionService.getTransactionById(eq(transactionId), eq(userId), eq(role))).thenReturn(transactionDto);

        // When & Then
        mockMvc.perform(get("/transactions/{transactionId}", transactionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", userId)
                        .header("X-User-Role", role))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1001L))
                .andExpect(jsonPath("$.accountId").value(101L))
                .andExpect(jsonPath("$.type").value("DEPOSIT"))
                .andExpect(jsonPath("$.amount").value(500.00));

        verify(transactionService, times(1)).getTransactionById(eq(transactionId), eq(userId), eq(role));
    }

    @Test
    @DisplayName("Should return 404 when transaction not found")
    void testGetTransaction_NotFound_Returns404() throws Exception {
        // Given
        Long transactionId = 9999L;
        Long userId = 1L;
        String role = "PERSON";
        when(transactionService.getTransactionById(eq(transactionId), eq(userId), eq(role)))
                .thenThrow(new TransactionNotFoundException("Transaction not found with ID: " + transactionId));

        // When & Then
        mockMvc.perform(get("/transactions/{transactionId}", transactionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", userId)
                        .header("X-User-Role", role))
                .andExpect(status().isNotFound());

        verify(transactionService, times(1)).getTransactionById(eq(transactionId), eq(userId), eq(role));
    }

    @Test
    @DisplayName("Should get transactions by account ID and return 200")
    void testGetTransactionsByAccount_Returns200() throws Exception {
        // Given
        Long accountId = 101L;
        Long userId = 1L;
        String role = "PERSON";
        
        TransactionDto tx1 = new TransactionDto();
        tx1.setId(1001L);
        tx1.setAccountId(accountId);
        tx1.setType("DEPOSIT");
        tx1.setAmount(new BigDecimal("500.00"));
        tx1.setTimestamp(now);
        tx1.setStatus("COMPLETED");

        TransactionDto tx2 = new TransactionDto();
        tx2.setId(1002L);
        tx2.setAccountId(accountId);
        tx2.setType("WITHDRAWAL");
        tx2.setAmount(new BigDecimal("100.00"));
        tx2.setTimestamp(now);
        tx2.setStatus("COMPLETED");

        List<TransactionDto> transactions = Arrays.asList(tx1, tx2);
        when(transactionService.getTransactionsByAccountId(eq(accountId), eq(userId), eq(role))).thenReturn(transactions);

        // When & Then
        mockMvc.perform(get("/transactions/account/{accountId}", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", userId)
                        .header("X-User-Role", role))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1001L))
                .andExpect(jsonPath("$[0].type").value("DEPOSIT"))
                .andExpect(jsonPath("$[1].id").value(1002L))
                .andExpect(jsonPath("$[1].type").value("WITHDRAWAL"));

        verify(transactionService, times(1)).getTransactionsByAccountId(eq(accountId), eq(userId), eq(role));
    }

    @Test
    @DisplayName("Should return empty list when no transactions found for account")
    void testGetTransactionsByAccount_EmptyList_Returns200() throws Exception {
        // Given
        Long accountId = 999L;
        Long userId = 1L;
        String role = "PERSON";
        when(transactionService.getTransactionsByAccountId(eq(accountId), eq(userId), eq(role))).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/transactions/account/{accountId}", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", userId)
                        .header("X-User-Role", role))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(transactionService, times(1)).getTransactionsByAccountId(eq(accountId), eq(userId), eq(role));
    }

    @Test
    @DisplayName("Should log transfer transaction with related account")
    void testLogTransaction_Transfer_Returns201() throws Exception {
        // Given
        logRequest.setTransactionType("TRANSFER");
        logRequest.setRelatedAccountId(102L);
        logRequest.setDescription("Transfer to account 102");

        transactionDto.setType("TRANSFER");
        transactionDto.setRelatedAccountId(102L);
        transactionDto.setDescription("Transfer to account 102");

        when(transactionService.logTransaction(any(LogTransactionRequest.class))).thenReturn(transactionDto);

        // When & Then
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("TRANSFER"))
                .andExpect(jsonPath("$.relatedAccountId").value(102L))
                .andExpect(jsonPath("$.description").value("Transfer to account 102"));

        verify(transactionService, times(1)).logTransaction(any(LogTransactionRequest.class));
    }
}
