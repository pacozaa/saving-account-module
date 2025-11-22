package com.banking.transfer.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign client for Register Service - PIN validation
 */
@FeignClient(name = "register-service")
public interface UserClient {

    @PostMapping("/register/validate-pin")
    Boolean validatePin(@RequestParam("userId") Long userId, @RequestParam("pin") String pin);
}
