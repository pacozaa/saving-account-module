package com.banking.deposit.client;

import com.banking.deposit.dto.LogTransactionRequest;
import com.banking.deposit.dto.TransactionDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "transaction-service")
public interface TransactionClient {

    @PostMapping("/transactions")
    TransactionDto logTransaction(@RequestBody LogTransactionRequest request);
}
