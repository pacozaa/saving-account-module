package com.banking.transfer.client;

import com.banking.transfer.dto.LogTransactionRequest;
import com.banking.transfer.dto.TransactionDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign client for Transaction Service
 */
@FeignClient(name = "transaction-service")
public interface TransactionClient {

    @PostMapping("/transactions")
    TransactionDto logTransaction(@RequestBody LogTransactionRequest request);
}
