package com.banking.register.client;

import com.banking.register.client.dto.AccountDto;
import com.banking.register.client.dto.CreateAccountRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "account-service")
public interface AccountClient {
    
    @PostMapping("/accounts/create")
    ResponseEntity<AccountDto> createAccount(@RequestBody CreateAccountRequest request);
}
