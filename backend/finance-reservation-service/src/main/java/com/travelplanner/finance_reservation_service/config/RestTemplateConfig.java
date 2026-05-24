package com.travelplanner.finance_reservation_service.config;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class RestTemplateConfig {

    private final HttpServletRequest request;

    @Bean
    public RestTemplate restTemplate() {

        RestTemplate restTemplate = new RestTemplate();

        restTemplate.getInterceptors().add(
                new ClientHttpRequestInterceptor() {

                    @Override
                    public ClientHttpResponse intercept(
                            HttpRequest req,
                            byte[] body,
                            ClientHttpRequestExecution execution
                    ) throws IOException {

                        String authHeader =
                                request.getHeader("Authorization");

                        if (authHeader != null) {

                            req.getHeaders().add(
                                    HttpHeaders.AUTHORIZATION,
                                    authHeader
                            );
                        }

                        return execution.execute(req, body);
                    }
                }
        );

        return restTemplate;
    }
}