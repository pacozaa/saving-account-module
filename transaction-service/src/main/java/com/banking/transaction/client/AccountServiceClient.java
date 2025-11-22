package com.banking.transaction.client;

import com.banking.transaction.dto.AccountDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "account-service")
public interface AccountServiceClient {
    
    @GetMapping("/accounts/{id}")
    AccountDto getAccountById(@PathVariable("id") String id);
}
