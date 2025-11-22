package com.banking.account.controller;

import com.banking.account.dto.AccountDto;
import com.banking.account.dto.CreateAccountRequest;
import com.banking.account.dto.UpdateBalanceRequest;
import com.banking.account.exception.AccountNotFoundException;
import com.banking.account.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
class AccountControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private AccountService accountService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private AccountDto testAccountDto;
    private CreateAccountRequest createRequest;
    
    @BeforeEach
    void setUp() {
        testAccountDto = AccountDto.builder()
            .id("1234567")
            .userId(1L)
            .balance(new BigDecimal("1000.00"))
            .accountType("SAVINGS")
            .createdAt(LocalDateTime.now())
            .build();
        
        createRequest = new CreateAccountRequest(1L, "1234567890123", "SAVINGS", new BigDecimal("500.00"));
    }
    
    @Test
    void testCreateAccount_ValidRequest_Returns201() throws Exception {
        // Given
        when(accountService.createAccount(any(CreateAccountRequest.class))).thenReturn(testAccountDto);
        
        // When & Then
        mockMvc.perform(post("/accounts/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1234567"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.balance").value(1000.00))
                .andExpect(jsonPath("$.accountType").value("SAVINGS"));
    }
    
    @Test
    void testCreateAccount_MissingUserId_Returns400() throws Exception {
        // Given
        CreateAccountRequest invalidRequest = new CreateAccountRequest(null, "1234567890123", "SAVINGS", new BigDecimal("500.00"));
        
        // When & Then
        mockMvc.perform(post("/accounts/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testCreateAccount_MissingAccountType_Returns400() throws Exception {
        // Given
        CreateAccountRequest invalidRequest = new CreateAccountRequest(1L, "1234567890123", "", new BigDecimal("500.00"));
        
        // When & Then
        mockMvc.perform(post("/accounts/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testCreateAccount_InvalidRequestBody_Returns500() throws Exception {
        // Given - Invalid JSON with string instead of Long for userId
        String invalidJson = "{\"userId\": \"invalid\", \"accountType\": \"SAVINGS\"}";
        
        // When & Then - JSON parsing errors result in 500 from global exception handler
        mockMvc.perform(post("/accounts/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isInternalServerError());
    }
    
    @Test
    void testGetAccount_ValidId_Returns200() throws Exception {
        // Given
        String accountId = "1234567";
        Long authenticatedUserId = 1L;
        when(accountService.getAccount(accountId, authenticatedUserId)).thenReturn(testAccountDto);
        
        // When & Then
        mockMvc.perform(get("/accounts/{id}", accountId)
                .header("X-User-Id", authenticatedUserId)
                .header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(accountId))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.balance").value(1000.00))
                .andExpect(jsonPath("$.accountType").value("SAVINGS"));
    }
    
    @Test
    void testGetAccount_NotFound_Returns404() throws Exception {
        // Given
        String accountId = "9999999";
        Long authenticatedUserId = 1L;
        when(accountService.getAccount(accountId, authenticatedUserId))
            .thenThrow(new AccountNotFoundException("Account not found with id: " + accountId));
        
        // When & Then
        mockMvc.perform(get("/accounts/{id}", accountId)
                .header("X-User-Id", authenticatedUserId)
                .header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void testUpdateBalance_ValidRequest_Returns200() throws Exception {
        // Given
        String accountId = "1234567";
        UpdateBalanceRequest updateRequest = new UpdateBalanceRequest(new BigDecimal("500.00"));
        
        AccountDto updatedAccount = AccountDto.builder()
            .id(accountId)
            .userId(1L)
            .balance(new BigDecimal("1500.00"))
            .accountType("SAVINGS")
            .createdAt(LocalDateTime.now())
            .build();
        
        when(accountService.updateBalance(eq(accountId), any(BigDecimal.class)))
            .thenReturn(updatedAccount);
        
        // When & Then
        mockMvc.perform(put("/accounts/{id}/balance", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(accountId))
                .andExpect(jsonPath("$.balance").value(1500.00));
    }
    
    @Test
    void testUpdateBalance_MissingAmount_Returns400() throws Exception {
        // Given
        String accountId = "1234567";
        String invalidJson = "{}";
        
        // When & Then
        mockMvc.perform(put("/accounts/{id}/balance", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testUpdateBalance_InsufficientFunds_Returns400() throws Exception {
        // Given
        String accountId = "1234567";
        UpdateBalanceRequest updateRequest = new UpdateBalanceRequest(new BigDecimal("-1500.00"));
        
        when(accountService.updateBalance(eq(accountId), any(BigDecimal.class)))
            .thenThrow(new IllegalArgumentException("Insufficient funds. Current balance: 1000.00"));
        
        // When & Then
        mockMvc.perform(put("/accounts/{id}/balance", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testGetAccountsByUserId_Returns200() throws Exception {
        // Given
        Long userId = 1L;
        Long authenticatedUserId = 1L;
        
        AccountDto account1 = AccountDto.builder()
            .id("1234567")
            .userId(userId)
            .balance(new BigDecimal("1000.00"))
            .accountType("SAVINGS")
            .createdAt(LocalDateTime.now())
            .build();
        
        AccountDto account2 = AccountDto.builder()
            .id("7654321")
            .userId(userId)
            .balance(new BigDecimal("2000.00"))
            .accountType("CHECKING")
            .createdAt(LocalDateTime.now())
            .build();
        
        List<AccountDto> accounts = Arrays.asList(account1, account2);
        
        when(accountService.getAccountsByUserId(userId, authenticatedUserId)).thenReturn(accounts);
        
        // When & Then
        mockMvc.perform(get("/accounts/user/{userId}", userId)
                .header("X-User-Id", authenticatedUserId)
                .header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value("1234567"))
                .andExpect(jsonPath("$[0].accountType").value("SAVINGS"))
                .andExpect(jsonPath("$[1].id").value("7654321"))
                .andExpect(jsonPath("$[1].accountType").value("CHECKING"));
    }
    
    @Test
    void testGetAccountsByUserId_EmptyList_Returns200() throws Exception {
        // Given
        Long userId = 999L;
        Long authenticatedUserId = 999L;
        
        when(accountService.getAccountsByUserId(userId, authenticatedUserId)).thenReturn(Arrays.asList());
        
        // When & Then
        mockMvc.perform(get("/accounts/user/{userId}", userId)
                .header("X-User-Id", authenticatedUserId)
                .header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
    
    @Test
    void testGetAccount_UnauthorizedAccess_Returns403() throws Exception {
        // Given - User 2 tries to access User 1's account
        String accountId = "1234567";
        Long authenticatedUserId = 2L;
        
        when(accountService.getAccount(accountId, authenticatedUserId))
            .thenThrow(new com.banking.account.exception.UnauthorizedAccessException(
                "You are not authorized to access this account"));
        
        // When & Then
        mockMvc.perform(get("/accounts/{id}", accountId)
                .header("X-User-Id", authenticatedUserId)
                .header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    void testGetAccountsByUserId_UnauthorizedAccess_Returns403() throws Exception {
        // Given - User 2 tries to access User 1's accounts
        Long userId = 1L;
        Long authenticatedUserId = 2L;
        
        when(accountService.getAccountsByUserId(userId, authenticatedUserId))
            .thenThrow(new com.banking.account.exception.UnauthorizedAccessException(
                "You are not authorized to access accounts of other users"));
        
        // When & Then
        mockMvc.perform(get("/accounts/user/{userId}", userId)
                .header("X-User-Id", authenticatedUserId)
                .header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    void testGetAccount_WithTellerRole_Returns403() throws Exception {
        // Given - TELLER tries to access an account
        String accountId = "1234567";
        Long authenticatedUserId = 1L;
        
        // When & Then
        mockMvc.perform(get("/accounts/{id}", accountId)
                .header("X-User-Id", authenticatedUserId)
                .header("X-User-Role", "TELLER"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Only customers are authorized to view account information"));
    }
    
    @Test
    void testGetAccount_WithPersonRole_Returns403() throws Exception {
        // Given - PERSON tries to access an account
        String accountId = "1234567";
        Long authenticatedUserId = 1L;
        
        // When & Then
        mockMvc.perform(get("/accounts/{id}", accountId)
                .header("X-User-Id", authenticatedUserId)
                .header("X-User-Role", "PERSON"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Only customers are authorized to view account information"));
    }
    
    @Test
    void testGetAccount_WithoutRole_Returns403() throws Exception {
        // Given - No role header
        String accountId = "1234567";
        Long authenticatedUserId = 1L;
        
        // When & Then
        mockMvc.perform(get("/accounts/{id}", accountId)
                .header("X-User-Id", authenticatedUserId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Only customers are authorized to view account information"));
    }
    
    @Test
    void testGetAccountsByUserId_WithTellerRole_Returns403() throws Exception {
        // Given - TELLER tries to access accounts
        Long userId = 1L;
        Long authenticatedUserId = 1L;
        
        // When & Then
        mockMvc.perform(get("/accounts/user/{userId}", userId)
                .header("X-User-Id", authenticatedUserId)
                .header("X-User-Role", "TELLER"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Only customers are authorized to view account information"));
    }
    
    @Test
    void testGetAccountsByUserId_WithPersonRole_Returns403() throws Exception {
        // Given - PERSON tries to access accounts
        Long userId = 1L;
        Long authenticatedUserId = 1L;
        
        // When & Then
        mockMvc.perform(get("/accounts/user/{userId}", userId)
                .header("X-User-Id", authenticatedUserId)
                .header("X-User-Role", "PERSON"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Only customers are authorized to view account information"));
    }
    
    @Test
    void testGetAccountsByUserId_WithoutRole_Returns403() throws Exception {
        // Given - No role header
        Long userId = 1L;
        Long authenticatedUserId = 1L;
        
        // When & Then
        mockMvc.perform(get("/accounts/user/{userId}", userId)
                .header("X-User-Id", authenticatedUserId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Only customers are authorized to view account information"));
    }
    
    @Test
    void testHealth_Returns200() throws Exception {
        // When & Then
        mockMvc.perform(get("/accounts/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Account Service is running"));
    }
}
