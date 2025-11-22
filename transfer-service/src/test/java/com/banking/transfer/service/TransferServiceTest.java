package com.banking.transfer.service;

import com.banking.transfer.client.AccountClient;
import com.banking.transfer.client.TransactionClient;
import com.banking.transfer.client.UserClient;
import com.banking.transfer.dto.*;
import com.banking.transfer.exception.AccountNotFoundException;
import com.banking.transfer.exception.InsufficientFundsException;
import com.banking.transfer.exception.SameAccountTransferException;
import com.banking.transfer.exception.UnauthorizedTransferException;
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
 * Unit tests for TransferService
 */
@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    private static final Long AUTHENTICATED_USER_ID = 1L;

    @Mock
    private AccountClient accountClient;

    @Mock
    private TransactionClient transactionClient;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private TransferService transferService;

    private TransferRequest transferRequest;
    private AccountDto senderAccount;
    private AccountDto receiverAccount;
    private AccountDto updatedSenderAccount;
    private AccountDto updatedReceiverAccount;
    private TransactionDto senderTransaction;
    private TransactionDto receiverTransaction;

    @BeforeEach
    void setUp() {
        // Setup transfer request
        transferRequest = new TransferRequest(
                101L,
                102L,
                new BigDecimal("500.00"),
                "123456",
                "Payment for services"
        );

        // Setup sender account
        senderAccount = AccountDto.builder()
                .id("101")
                .userId(1L)
                .balance(new BigDecimal("1500.00"))
                .accountType("SAVINGS")
                .createdAt(LocalDateTime.now())
                .build();

        // Setup receiver account
        receiverAccount = AccountDto.builder()
                .id("102")
                .userId(2L)
                .balance(new BigDecimal("1000.00"))
                .accountType("SAVINGS")
                .createdAt(LocalDateTime.now())
                .build();

        // Setup updated sender account (after deduction)
        updatedSenderAccount = AccountDto.builder()
                .id("101")
                .userId(1L)
                .balance(new BigDecimal("1000.00"))
                .accountType("SAVINGS")
                .createdAt(LocalDateTime.now())
                .build();

        // Setup updated receiver account (after credit)
        updatedReceiverAccount = AccountDto.builder()
                .id("102")
                .userId(2L)
                .balance(new BigDecimal("1500.00"))
                .accountType("SAVINGS")
                .createdAt(LocalDateTime.now())
                .build();

        // Setup sender transaction
        senderTransaction = TransactionDto.builder()
                .id(1001L)
                .accountId(101L)
                .transactionType("TRANSFER_OUT")
                .amount(new BigDecimal("500.00"))
                .relatedAccountId(102L)
                .description("Payment for services")
                .timestamp(LocalDateTime.now())
                .build();

        // Setup receiver transaction
        receiverTransaction = TransactionDto.builder()
                .id(1002L)
                .accountId(102L)
                .transactionType("TRANSFER_IN")
                .amount(new BigDecimal("500.00"))
                .relatedAccountId(101L)
                .description("Payment for services")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Test
    void testTransfer_Success_UpdatesBothAccountsAndLogsTransactions() {
        // Given
        when(userClient.validatePin(AUTHENTICATED_USER_ID, "123456")).thenReturn(true);
        when(accountClient.getAccount("101")).thenReturn(senderAccount);
        when(accountClient.getAccount("102")).thenReturn(receiverAccount);
        when(accountClient.updateBalance(eq("101"), any(UpdateBalanceRequest.class)))
                .thenReturn(updatedSenderAccount);
        when(accountClient.updateBalance(eq("102"), any(UpdateBalanceRequest.class)))
                .thenReturn(updatedReceiverAccount);
        when(transactionClient.logTransaction(any(LogTransactionRequest.class)))
                .thenReturn(senderTransaction)
                .thenReturn(receiverTransaction);

        // When
        TransferResponse response = transferService.transfer(transferRequest, AUTHENTICATED_USER_ID);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTransactionId()).isEqualTo(1001L);
        assertThat(response.getFromAccountId()).isEqualTo(101L);
        assertThat(response.getToAccountId()).isEqualTo(102L);
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(response.getFromAccountNewBalance()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(response.getToAccountNewBalance()).isEqualByComparingTo(new BigDecimal("1500.00"));
        assertThat(response.getMessage()).isEqualTo("Transfer successful");

        // Verify interactions
        verify(accountClient).getAccount("101");
        verify(accountClient).getAccount("102");
        verify(accountClient).updateBalance(eq("101"), any(UpdateBalanceRequest.class));
        verify(accountClient).updateBalance(eq("102"), any(UpdateBalanceRequest.class));
        verify(transactionClient, times(2)).logTransaction(any(LogTransactionRequest.class));
    }

    @Test
    void testTransfer_InsufficientFunds_ThrowsException() {
        // Given - sender has insufficient balance
        AccountDto poorSenderAccount = AccountDto.builder()
                .id("101")
                .userId(1L)
                .balance(new BigDecimal("100.00"))
                .accountType("SAVINGS")
                .createdAt(LocalDateTime.now())
                .build();

        when(userClient.validatePin(AUTHENTICATED_USER_ID, "123456")).thenReturn(true);
        when(accountClient.getAccount("101")).thenReturn(poorSenderAccount);

        // When & Then
        assertThatThrownBy(() -> transferService.transfer(transferRequest, AUTHENTICATED_USER_ID))
                .isInstanceOf(InsufficientFundsException.class);

        // Verify that we only fetched the sender account and didn't proceed further
        verify(accountClient).getAccount("101");
        verify(accountClient, never()).getAccount("102");
        verify(accountClient, never()).updateBalance(anyString(), any());
        verify(transactionClient, never()).logTransaction(any());
    }

    @Test
    void testTransfer_SameAccount_ThrowsException() {
        // Given - same account transfer
        TransferRequest sameAccountRequest = new TransferRequest(
                101L,
                101L,
                new BigDecimal("500.00"),
                "123456",
                "Same account transfer"
        );

        when(userClient.validatePin(AUTHENTICATED_USER_ID, "123456")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> transferService.transfer(sameAccountRequest, AUTHENTICATED_USER_ID))
                .isInstanceOf(SameAccountTransferException.class);

        // Verify that no external calls were made
        verify(accountClient, never()).getAccount(anyString());
        verify(accountClient, never()).updateBalance(anyString(), any());
        verify(transactionClient, never()).logTransaction(any());
    }

    @Test
    void testTransfer_SenderAccountNotFound_ThrowsException() {
        // Given - sender account not found
        Request request = Request.create(Request.HttpMethod.GET, "/accounts/101",
                new HashMap<>(), null, new RequestTemplate());
        FeignException.NotFound notFound = new FeignException.NotFound(
                "Account not found", request, null, null);

        when(userClient.validatePin(AUTHENTICATED_USER_ID, "123456")).thenReturn(true);
        when(accountClient.getAccount("101")).thenThrow(notFound);

        // When & Then
        assertThatThrownBy(() -> transferService.transfer(transferRequest, AUTHENTICATED_USER_ID))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("101");

        // Verify that we only tried to fetch the sender account
        verify(accountClient).getAccount("101");
        verify(accountClient, never()).getAccount("102");
        verify(accountClient, never()).updateBalance(anyString(), any());
        verify(transactionClient, never()).logTransaction(any());
    }

    @Test
    void testTransfer_ReceiverAccountNotFound_ThrowsException() {
        // Given - receiver account not found
        when(userClient.validatePin(AUTHENTICATED_USER_ID, "123456")).thenReturn(true);
        when(accountClient.getAccount("101")).thenReturn(senderAccount);

        Request request = Request.create(Request.HttpMethod.GET, "/accounts/102",
                new HashMap<>(), null, new RequestTemplate());
        FeignException.NotFound notFound = new FeignException.NotFound(
                "Account not found", request, null, null);

        when(accountClient.getAccount("102")).thenThrow(notFound);

        // When & Then
        assertThatThrownBy(() -> transferService.transfer(transferRequest, AUTHENTICATED_USER_ID))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("102");

        // Verify interactions
        verify(accountClient).getAccount("101");
        verify(accountClient).getAccount("102");
        verify(accountClient, never()).updateBalance(anyString(), any());
        verify(transactionClient, never()).logTransaction(any());
    }

    @Test
    void testTransfer_UnauthorizedUser_ThrowsException() {
        // Given - user trying to transfer from account they don't own
        Long unauthorizedUserId = 999L;
        when(userClient.validatePin(unauthorizedUserId, "123456")).thenReturn(true);
        when(accountClient.getAccount("101")).thenReturn(senderAccount);

        // When & Then
        assertThatThrownBy(() -> transferService.transfer(transferRequest, unauthorizedUserId))
                .isInstanceOf(UnauthorizedTransferException.class)
                .hasMessageContaining("999")
                .hasMessageContaining("101");

        // Verify that we only fetched the sender account and didn't proceed further
        verify(accountClient).getAccount("101");
        verify(accountClient, never()).getAccount("102");
        verify(accountClient, never()).updateBalance(anyString(), any());
        verify(transactionClient, never()).logTransaction(any());
    }
}
