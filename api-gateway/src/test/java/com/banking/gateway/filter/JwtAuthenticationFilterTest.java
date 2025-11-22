package com.banking.gateway.filter;

import com.banking.gateway.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JwtAuthenticationFilter
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private GatewayFilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        // Setup will be done per test as needed
    }

    @Test
    void testFilter_PublicRoute_Login_SkipsAuthentication() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/auth/login")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // When
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(filterChain).filter(exchange);
        verify(jwtUtil, never()).validateToken(anyString());
    }

    @Test
    void testFilter_PublicRoute_Register_SkipsAuthentication() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/register")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // When
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(filterChain).filter(exchange);
        verify(jwtUtil, never()).validateToken(anyString());
    }

    @Test
    void testFilter_PublicRoute_Actuator_SkipsAuthentication() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/actuator/health")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // When
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(filterChain).filter(exchange);
        verify(jwtUtil, never()).validateToken(anyString());
    }

    @Test
    void testFilter_MissingAuthorizationHeader_Returns401() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/accounts")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        // When
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        ServerHttpResponse response = exchange.getResponse();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(filterChain, never()).filter(any(ServerWebExchange.class));
    }

    @Test
    void testFilter_InvalidAuthorizationHeader_NoBearer_Returns401() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/accounts")
                .header(HttpHeaders.AUTHORIZATION, "InvalidToken")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        // When
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        ServerHttpResponse response = exchange.getResponse();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(filterChain, never()).filter(any(ServerWebExchange.class));
    }

    @Test
    void testFilter_InvalidToken_Returns401() {
        // Given
        String invalidToken = "invalid.jwt.token";
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/accounts")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtUtil.validateToken(invalidToken)).thenReturn(false);

        // When
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        ServerHttpResponse response = exchange.getResponse();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(jwtUtil).validateToken(invalidToken);
        verify(filterChain, never()).filter(any(ServerWebExchange.class));
    }

    @Test
    void testFilter_ValidToken_AddsHeadersAndProceedsRequest() {
        // Given
        String validToken = "valid.jwt.token";
        Long userId = 123L;
        String username = "john.doe";
        String role = "CUSTOMER";

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/accounts")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(jwtUtil.extractUserId(validToken)).thenReturn(userId);
        when(jwtUtil.extractUsername(validToken)).thenReturn(username);
        when(jwtUtil.extractRole(validToken)).thenReturn(role);
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // When
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        // Verify JWT methods were called
        verify(jwtUtil).validateToken(validToken);
        verify(jwtUtil).extractUserId(validToken);
        verify(jwtUtil).extractUsername(validToken);
        verify(jwtUtil).extractRole(validToken);

        // Verify filter chain was called with modified exchange
        verify(filterChain).filter(argThat(modifiedExchange -> {
            ServerHttpRequest modifiedRequest = modifiedExchange.getRequest();
            HttpHeaders headers = modifiedRequest.getHeaders();
            
            return headers.getFirst("X-User-Id").equals(String.valueOf(userId)) &&
                   headers.getFirst("X-Username").equals(username) &&
                   headers.getFirst("X-User-Role").equals(role);
        }));
    }

    @Test
    void testFilter_ValidToken_TellerRole_AddsCorrectHeaders() {
        // Given
        String validToken = "valid.jwt.token";
        Long userId = 456L;
        String username = "teller.user";
        String role = "TELLER";

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/deposit")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(jwtUtil.extractUserId(validToken)).thenReturn(userId);
        when(jwtUtil.extractUsername(validToken)).thenReturn(username);
        when(jwtUtil.extractRole(validToken)).thenReturn(role);
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // When
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(jwtUtil).validateToken(validToken);
        verify(filterChain).filter(argThat(modifiedExchange -> {
            ServerHttpRequest modifiedRequest = modifiedExchange.getRequest();
            HttpHeaders headers = modifiedRequest.getHeaders();
            
            return headers.getFirst("X-User-Role").equals("TELLER");
        }));
    }

    @Test
    void testFilter_TokenValidationThrowsException_Returns401() {
        // Given
        String token = "problematic.token";
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/accounts")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtUtil.validateToken(token)).thenThrow(new RuntimeException("Token parsing error"));

        // When
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        ServerHttpResponse response = exchange.getResponse();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(filterChain, never()).filter(any(ServerWebExchange.class));
    }

    @Test
    void testFilter_PublicRoute_SwaggerUI_SkipsAuthentication() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/swagger-ui/index.html")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // When
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(filterChain).filter(exchange);
        verify(jwtUtil, never()).validateToken(anyString());
    }

    @Test
    void testFilter_PublicRoute_ApiDocs_SkipsAuthentication() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/v3/api-docs")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // When
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(filterChain).filter(exchange);
        verify(jwtUtil, never()).validateToken(anyString());
    }

    @Test
    void testGetOrder_ReturnsNegativeValue() {
        // When
        int order = jwtAuthenticationFilter.getOrder();

        // Then
        assertThat(order).isEqualTo(-100);
    }

    @Test
    void testFilter_EmptyBearerToken_Returns401() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/accounts")
                .header(HttpHeaders.AUTHORIZATION, "Bearer ")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtUtil.validateToken("")).thenReturn(false);

        // When
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        ServerHttpResponse response = exchange.getResponse();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
