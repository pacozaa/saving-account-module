package com.banking.transfer.client;

import com.banking.transfer.dto.AccountDto;
import com.banking.transfer.dto.UpdateBalanceRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign client for Account Service
 */
@FeignClient(name = "account-service")
public interface AccountClient {

    @GetMapping("/accounts/{id}")
    AccountDto getAccount(@PathVariable("id") String id);

    @PutMapping("/accounts/{id}/balance")
    AccountDto updateBalance(@PathVariable("id") String id, @RequestBody UpdateBalanceRequest request);
}
