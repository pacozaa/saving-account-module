package com.banking.deposit.client;

import com.banking.deposit.dto.AccountDto;
import com.banking.deposit.dto.UpdateBalanceRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "account-service")
public interface AccountClient {

    @GetMapping("/accounts/{id}")
    AccountDto getAccount(@PathVariable("id") String id);

    @PutMapping("/accounts/{id}/balance")
    AccountDto updateBalance(
            @PathVariable("id") String id,
            @RequestBody UpdateBalanceRequest request
    );
}
