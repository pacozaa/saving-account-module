package com.banking.transfer.controller;

import com.banking.transfer.dto.TransferRequest;
import com.banking.transfer.dto.TransferResponse;
import com.banking.transfer.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transfer")
@Tag(name = "Transfer Operations", description = "Endpoints for fund transfer orchestration")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class TransferController {

    private final TransferService transferService;

    @Operation(
            summary = "Transfer Funds",
            description = "Transfers money from one account to another. " +
                    "Coordinates with Account Service to deduct from source, credit to destination, " +
                    "and Transaction Service to log the transfer."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Transfer successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TransferResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data or insufficient funds"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account not found"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    @PostMapping
    public ResponseEntity<TransferResponse> transfer(
            @Valid @RequestBody TransferRequest request
    ) {
        log.info("POST /transfer - from: {}, to: {}, amount: {}", 
                request.getFromAccountId(), request.getToAccountId(), request.getAmount());
        
        TransferResponse response = transferService.transfer(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Health Check",
            description = "Returns the health status of the transfer service"
    )
    @ApiResponse(responseCode = "200", description = "Service is healthy")
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Transfer Service is running");
    }
}
