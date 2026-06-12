package com.techieprogramme.security;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter
                implements GlobalFilter {

        private final JwtService jwtService;

        @Override
        public Mono<Void> filter(
                        ServerWebExchange exchange,
                        GatewayFilterChain chain) {

                ServerHttpRequest request = exchange.getRequest();

                String path = request.getURI().getPath();

                System.out.println("PATH: " + path);
                // Allow auth APIs, SSE notifications and browser preflight
                if (HttpMethod.OPTIONS.equals(request.getMethod())
                                || path.startsWith("/auth")
                                || path.startsWith("/api/notification/stream")
                                || path.startsWith("/api/notification/recent")) {
                        return chain.filter(exchange);
                }

                // Check Authorization header
                if (!request.getHeaders()
                                .containsKey(HttpHeaders.AUTHORIZATION)) {

                        System.out.println("No Authorization Header");

                        exchange.getResponse()
                                        .setStatusCode(HttpStatus.UNAUTHORIZED);

                        return exchange.getResponse().setComplete();
                }

                String authHeader = request.getHeaders()
                                .getFirst(HttpHeaders.AUTHORIZATION);

                System.out.println(authHeader);

                if (authHeader == null ||
                                !authHeader.startsWith("Bearer ")) {

                        System.out.println("Invalid Bearer Token");

                        exchange.getResponse()
                                        .setStatusCode(HttpStatus.UNAUTHORIZED);

                        return exchange.getResponse().setComplete();
                }

                String token = authHeader.substring(7);

                boolean valid = jwtService.validateToken(token);

                System.out.println("TOKEN VALID: " + valid);

                if (!valid) {

                        exchange.getResponse()
                                        .setStatusCode(HttpStatus.UNAUTHORIZED);

                        return exchange.getResponse().setComplete();
                }

                return chain.filter(exchange);
        }
}