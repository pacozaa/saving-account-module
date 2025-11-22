package com.banking.register.controller;

import com.banking.register.dto.RegisterRequest;
import com.banking.register.dto.RegisterResponse;
import com.banking.register.dto.UserDto;
import com.banking.register.entity.UserRole;
import com.banking.register.exception.UserAlreadyExistsException;
import com.banking.register.service.RegisterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RegisterController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Register Controller Tests")
class RegisterControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private RegisterService registerService;
    
    private RegisterRequest validRequest;
    private RegisterResponse registerResponse;
    private UserDto userDto;
    
    @BeforeEach
    void setUp() {
        validRequest = RegisterRequest.builder()
                .username("john_doe")
                .password("password123")
                .email("john.doe@example.com")
                .role(UserRole.CUSTOMER)
                .build();
        
        userDto = UserDto.builder()
                .id(1L)
                .username("john_doe")
                .password("$2a$10$hashedPassword")
                .email("john.doe@example.com")
                .role(UserRole.CUSTOMER)
                .registeredAt(LocalDateTime.now())
                .build();
        
        registerResponse = RegisterResponse.builder()
                .user(userDto)
                .defaultAccountId(100L)
                .message("User registered successfully with default account")
                .build();
    }
    
    @Test
    @DisplayName("POST /register - Should register user and return 201")
    void testRegisterUser_ValidRequest_Returns201() throws Exception {
        // Given
        when(registerService.registerUser(any(RegisterRequest.class))).thenReturn(registerResponse);
        
        // When & Then
        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user").exists())
                .andExpect(jsonPath("$.user.username").value("john_doe"))
                .andExpect(jsonPath("$.user.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.user.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.defaultAccountId").value(100))
                .andExpect(jsonPath("$.message").exists());
    }
    
    @Test
    @DisplayName("POST /register - Should return 409 when username already exists")
    void testRegisterUser_DuplicateUsername_Returns409() throws Exception {
        // Given
        when(registerService.registerUser(any(RegisterRequest.class)))
                .thenThrow(new UserAlreadyExistsException("Username 'john_doe' is already taken"));
        
        // When & Then
        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict());
    }
    
    @Test
    @DisplayName("GET /register/user/{username} - Should return user by username")
    void testGetUserByUsername_ValidUsername_Returns200() throws Exception {
        // Given
        when(registerService.getUserByUsername("john_doe")).thenReturn(userDto);
        
        // When & Then
        mockMvc.perform(get("/register/user/john_doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("john_doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }
    
    @Test
    @DisplayName("GET /register/user/id/{userId} - Should return user by ID")
    void testGetUserById_ValidId_Returns200() throws Exception {
        // Given
        when(registerService.getUserById(1L)).thenReturn(userDto);
        
        // When & Then
        mockMvc.perform(get("/register/user/id/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("john_doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }
    
    @Test
    @DisplayName("GET /register/health - Should return health check")
    void testHealth_ReturnsOk() throws Exception {
        mockMvc.perform(get("/register/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Register Service is running"));
    }
}
