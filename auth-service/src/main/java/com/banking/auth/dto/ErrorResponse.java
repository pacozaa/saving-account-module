package com.banking.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Error response for API failures")
public class ErrorResponse {

    @Schema(description = "Error code", example = "AUTH_001")
    private String code;

    @Schema(description = "Error message", example = "Invalid credentials")
    private String message;

    @Schema(description = "Timestamp of the error", example = "2023-12-01T10:30:00Z")
    private String timestamp;
}
