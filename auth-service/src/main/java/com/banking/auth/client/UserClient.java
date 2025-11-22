package com.banking.auth.client;

import com.banking.auth.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "register-service")
public interface UserClient {

    /**
     * Get user by username from Register Service
     * 
     * @param username Username to look up
     * @return UserDto containing user information
     */
    @GetMapping("/register/user/{username}")
    UserDto getUserByUsername(@PathVariable("username") String username);
}
