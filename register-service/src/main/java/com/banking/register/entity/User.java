package com.banking.register.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    
    @Column(nullable = false)
    private String password; // BCrypt hashed
    
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(nullable = false, unique = true, length = 13)
    private String citizenId; // Thai national ID (13 digits)
    
    @Column(nullable = false, length = 100)
    private String thaiName;
    
    @Column(nullable = false, length = 100)
    private String englishName;
    
    @Column(nullable = false, length = 255)
    private String pin; // BCrypt hashed 6-digit PIN
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private UserRole role; // PERSON, CUSTOMER, TELLER
    
    @Column(name = "registered_at", nullable = false, updatable = false)
    private LocalDateTime registeredAt;
    
    @PrePersist
    protected void onCreate() {
        registeredAt = LocalDateTime.now();
    }
}
