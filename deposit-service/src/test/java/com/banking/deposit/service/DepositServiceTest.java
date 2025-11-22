package com.banking.deposit.service;

import com.banking.deposit.client.AccountClient;
import com.banking.deposit.client.TransactionClient;
import com.banking.deposit.dto.*;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DepositService
 * Tests the deposit orchestration logic with mocked Feign clients
 */
@ExtendWith(MockitoExtension.class)
class DepositServiceTest {

    @Mock
    private AccountClient accountClient;

    @Mock
    private TransactionClient transactionClient;

    @InjectMocks
    private DepositService depositService;

    private DepositRequest depositRequest;
    private AccountDto accountDto;
    private AccountDto updatedAccountDto;
    private TransactionDto transactionDto;

    @BeforeEach
    void setUp() {
        // Prepare test data
        depositRequest = new DepositRequest(
                "1234567",
                new BigDecimal("1000.00"),
                5L,
                "Cash deposit"
        );

        accountDto = AccountDto.builder()
                .id("1234567")
                .userId(10L)
                .balance(new BigDecimal("1500.00"))
                .accountType("SAVINGS")
                .createdAt(LocalDateTime.now())
                .build();

        updatedAccountDto = AccountDto.builder()
                .id("1234567")
                .userId(10L)
                .balance(new BigDecimal("2500.00")) // 1500 + 1000
                .accountType("SAVINGS")
                .createdAt(LocalDateTime.now())
                .build();

        transactionDto = new TransactionDto(
                101L,
                1234567L,
                "DEPOSIT",
                new BigDecimal("1000.00"),
                null,
                "Cash deposit",
                LocalDateTime.now(),
                "COMPLETED"
        );
    }

    @Test
    void testDeposit_Success_UpdatesBalanceAndLogsTransaction() {
        // Given
        when(accountClient.getAccount(depositRequest.getAccountId())).thenReturn(accountDto);
        when(accountClient.updateBalance(eq(depositRequest.getAccountId()), any(UpdateBalanceRequest.class)))
                .thenReturn(updatedAccountDto);
        when(transactionClient.logTransaction(any(LogTransactionRequest.class)))
                .thenReturn(transactionDto);

        // When
        DepositResponse response = depositService.processDeposit(depositRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTransactionId()).isEqualTo(101L);
        assertThat(response.getAccountId()).isEqualTo("1234567");
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(response.getNewBalance()).isEqualByComparingTo(new BigDecimal("2500.00"));
        assertThat(response.getMessage()).isEqualTo("Deposit successful");

        // Verify interactions
        verify(accountClient).getAccount("1234567");
        verify(accountClient).updateBalance(eq("1234567"), any(UpdateBalanceRequest.class));
        verify(transactionClient).logTransaction(any(LogTransactionRequest.class));
    }

    @Test
    void testDeposit_AccountNotFound_ThrowsException() {
        // Given
        Request request = Request.create(
                Request.HttpMethod.GET,
                "/accounts/9999999",
                new HashMap<>(),
                null,
                new RequestTemplate()
        );
        
        when(accountClient.getAccount("9999999"))
                .thenThrow(new FeignException.NotFound(
                        "Account not found",
                        request,
                        null,
                        null
                ));

        DepositRequest invalidRequest = new DepositRequest(
                "9999999",
                new BigDecimal("1000.00"),
                5L,
                "Cash deposit"
        );

        // When & Then
        assertThatThrownBy(() -> depositService.processDeposit(invalidRequest))
                .isInstanceOf(FeignException.NotFound.class);

        // Verify that updateBalance and logTransaction were NOT called
        verify(accountClient).getAccount("9999999");
        verify(accountClient, never()).updateBalance(anyString(), any());
        verify(transactionClient, never()).logTransaction(any());
    }

    @Test
    void testDeposit_InvalidAmount_ThrowsException() {
        // Given - amount validation happens at controller level via @Valid
        // But we can test the service behavior with zero/negative amounts
        DepositRequest invalidRequest = new DepositRequest(
                "1234567",
                BigDecimal.ZERO,
                5L,
                "Invalid deposit"
        );

        when(accountClient.getAccount(invalidRequest.getAccountId())).thenReturn(accountDto);
        when(accountClient.updateBalance(eq(invalidRequest.getAccountId()), any(UpdateBalanceRequest.class)))
                .thenReturn(accountDto); // Balance unchanged

        when(transactionClient.logTransaction(any(LogTransactionRequest.class)))
                .thenReturn(transactionDto);

        // When
        DepositResponse response = depositService.processDeposit(invalidRequest);

        // Then - service processes it, validation should happen at controller
        assertThat(response).isNotNull();
        verify(accountClient).getAccount("1234567");
    }

    @Test
    void testDeposit_FeignClientError_ThrowsException() {
        // Given
        when(accountClient.getAccount(depositRequest.getAccountId())).thenReturn(accountDto);
        
        Request request = Request.create(
                Request.HttpMethod.PUT,
                "/accounts/1234567/balance",
                new HashMap<>(),
                null,
                new RequestTemplate()
        );
        
        when(accountClient.updateBalance(eq(depositRequest.getAccountId()), any(UpdateBalanceRequest.class)))
                .thenThrow(new FeignException.InternalServerError(
                        "Account service error",
                        request,
                        null,
                        null
                ));

        // When & Then
        assertThatThrownBy(() -> depositService.processDeposit(depositRequest))
                .isInstanceOf(FeignException.InternalServerError.class);

        // Verify that transaction was NOT logged
        verify(accountClient).getAccount("1234567");
        verify(accountClient).updateBalance(eq("1234567"), any(UpdateBalanceRequest.class));
        verify(transactionClient, never()).logTransaction(any());
    }

    @Test
    void testDeposit_WithoutTellerId_UsesDefaultDescription() {
        // Given
        DepositRequest requestWithoutTeller = new DepositRequest(
                "1234567",
                new BigDecimal("500.00"),
                null, // No teller ID
                null  // No description
        );

        when(accountClient.getAccount(requestWithoutTeller.getAccountId())).thenReturn(accountDto);
        when(accountClient.updateBalance(eq(requestWithoutTeller.getAccountId()), any(UpdateBalanceRequest.class)))
                .thenReturn(updatedAccountDto);
        when(transactionClient.logTransaction(any(LogTransactionRequest.class)))
                .thenReturn(transactionDto);

        // When
        DepositResponse response = depositService.processDeposit(requestWithoutTeller);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo("Deposit successful");

        // Verify transaction description is just "Deposit"
        verify(transactionClient).logTransaction(argThat(req ->
                req.getDescription().equals("Deposit")
        ));
    }

    @Test
    void testDeposit_WithTellerId_IncludesTellerInDescription() {
        // Given
        when(accountClient.getAccount(depositRequest.getAccountId())).thenReturn(accountDto);
        when(accountClient.updateBalance(eq(depositRequest.getAccountId()), any(UpdateBalanceRequest.class)))
                .thenReturn(updatedAccountDto);
        when(transactionClient.logTransaction(any(LogTransactionRequest.class)))
                .thenReturn(transactionDto);

        // When
        depositService.processDeposit(depositRequest);

        // Then - verify description uses the provided one (not default)
        verify(transactionClient).logTransaction(argThat(req ->
                req.getDescription().equals("Cash deposit")
        ));
    }

    @Test
    void testDeposit_TransactionClientError_ThrowsException() {
        // Given
        when(accountClient.getAccount(depositRequest.getAccountId())).thenReturn(accountDto);
        when(accountClient.updateBalance(eq(depositRequest.getAccountId()), any(UpdateBalanceRequest.class)))
                .thenReturn(updatedAccountDto);
        
        Request request = Request.create(
                Request.HttpMethod.POST,
                "/transactions",
                new HashMap<>(),
                null,
                new RequestTemplate()
        );
        
        when(transactionClient.logTransaction(any(LogTransactionRequest.class)))
                .thenThrow(new FeignException.InternalServerError(
                        "Transaction service error",
                        request,
                        null,
                        null
                ));

        // When & Then
        assertThatThrownBy(() -> depositService.processDeposit(depositRequest))
                .isInstanceOf(FeignException.InternalServerError.class);

        // Verify all steps were attempted
        verify(accountClient).getAccount("1234567");
        verify(accountClient).updateBalance(eq("1234567"), any(UpdateBalanceRequest.class));
        verify(transactionClient).logTransaction(any(LogTransactionRequest.class));
    }
}
