package com.travelplanner.api_gateway.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.travelplanner.api_gateway.util.JwtUtils;

import io.jsonwebtoken.ExpiredJwtException;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private JwtUtils jwtUtils;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();
            HttpMethod method = exchange.getRequest().getMethod();

            // Allow browser CORS preflight requests through without auth.
            if (HttpMethod.OPTIONS.equals(method)) {
                return chain.filter(exchange);
            }

            // Skip authentication for public auth endpoints.
            boolean isPublicAuthRequest =
                    (path.matches("^/api/users/?$") && HttpMethod.POST.equals(method))
                    || path.contains("/api/users/login")
                    || path.contains("/api/users/register")
                    || path.contains("/api/users/refresh");

            if (isPublicAuthRequest) {

                return chain.filter(exchange);
            }
            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
            String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                authHeader = authHeader.substring(7);
            }

            try {
                jwtUtils.validateToken(authHeader);

                String role = jwtUtils.extractRole(authHeader);

                String type = jwtUtils.extractType(authHeader);

                if (!type.equals("access")) {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }

                if(path.contains("/api/users/admin") && !role.equals("ADMIN")) {
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }

            } catch (ExpiredJwtException e) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                byte[] bytes = "Token expired".getBytes();
                return exchange.getResponse().writeWith(reactor.core.publisher.Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
            } catch (Exception e) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                byte[] bytes = "Unauthorized access".getBytes();
                return exchange.getResponse().writeWith(reactor.core.publisher.Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
            }
            return chain.filter(exchange);
        });
    }

    public static class Config {}
}
