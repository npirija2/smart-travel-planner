package com.travelplanner.communication_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class FeignConfig {

    private final HttpServletRequest request;

    @Bean
    public RequestInterceptor requestInterceptor() {

        return template -> {

            String authHeader =
                    request.getHeader("Authorization");

            if (authHeader != null) {
                template.header(
                        "Authorization",
                        authHeader
                );
            }
        };
    }
}