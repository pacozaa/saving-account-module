package com.banking.transaction.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "register-service")
public interface RegisterServiceClient {
    
    @PostMapping("/register/validate-pin")
    Boolean validatePin(@RequestParam("userId") Long userId, @RequestParam("pin") String pin);
}
