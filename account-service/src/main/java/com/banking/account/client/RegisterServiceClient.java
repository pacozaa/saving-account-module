package com.banking.account.client;

import com.banking.account.client.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "register-service")
public interface RegisterServiceClient {
    
    @GetMapping("/register/user/id/{userId}")
    UserDto getUserById(@PathVariable("userId") Long userId);
}
