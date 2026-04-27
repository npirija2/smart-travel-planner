package com.travelplanner.planning_service;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

public class LoadBalanceTest {

    @Test
    void testLoadBalancing() {

        RestTemplate restTemplate = new RestTemplate();

        int count8082 = 0;
        int count8083 = 0;

        long start = System.currentTimeMillis();

        for (int i = 0; i < 100; i++) {

            String url = (i % 2 == 0)
                    ? "http://localhost:8082/api/travel-plans/test"
                    : "http://localhost:8083/api/travel-plans/test";

            String response = restTemplate.getForObject(url, String.class);

            if (response.contains("8082")) count8082++;
            if (response.contains("8083")) count8083++;
        }

        long durationLB = System.currentTimeMillis() - start;

        System.out.println("=== SA LOAD BALANCINGOM ===");
        System.out.println("Instanca 8082: " + count8082);
        System.out.println("Instanca 8083: " + count8083);
        System.out.println("Ukupno vrijeme: " + durationLB + " ms");

        int directCount = 0;

        start = System.currentTimeMillis();

        for (int i = 0; i < 100; i++) {
            String response = restTemplate.getForObject(
                "http://localhost:8082/api/travel-plans/test",
                String.class
            );

            if (response.contains("8082")) directCount++;
        }

        long durationNoLB = System.currentTimeMillis() - start;

        System.out.println("\n=== BEZ LOAD BALANCINGA ===");
        System.out.println("Instanca 8082: " + directCount);
        System.out.println("Ukupno vrijeme: " + durationNoLB + " ms");
    }
}