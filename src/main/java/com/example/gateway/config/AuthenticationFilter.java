package com.example.gateway.config;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Date;

@RefreshScope
@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    public AuthenticationFilter() {
        super(Config.class);
    }
    public static final String SECRET = "gooloussamagharianitothisprojectwillbenumberone";

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // Extract token from request
            String token = extractTokenFromRequest(exchange);

            // Validate token against authentication service
            return validateToken(token)
                    .flatMap(isValidToken -> {
                        if (isValidToken) {
                            // Token is valid, allow request to proceed
                            return chain.filter(exchange);
                        } else {
                            // Token is invalid, reject request with unauthorized response
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        }
                    });
        };
    }

    private Mono<Boolean> validateToken(String token) {
        // Simulate token validation logic (e.g., call authentication service)
        if (token != null && validateToken(token,SECRET)) {
            return Mono.just(true); // Token is valid
        } else {
            return Mono.just(false); // Token is invalid
        }
    }
    public boolean validateToken(String token, String secretKey) {
        try {
            // Parse the token and verify its signature
            Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);

            // Check expiration
            if (claims.getBody().getExpiration().before(new Date())) {
                // Token has expired
                return false;
            }

            // Additional validation steps (e.g., check user permissions, issuer, etc.)

            return true; // Token is valid
        } catch (ExpiredJwtException ex) {
            // Token has expired
            return false;
        } catch (Exception ex) {
            // Token validation failed for other reasons (e.g., invalid signature, malformed token)
            return false;
        }
    }
    private String extractTokenFromRequest(ServerWebExchange exchange) {
        // Extract token from request headers, query parameters, etc.
        // For demonstration, let's assume the token is in the Authorization header
        String authorizationHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7); // Remove "Bearer " prefix
        }
        return null; // Token not found or invalid format
    }

    public static class Config {
        // You can add configuration properties here if needed
    }
}