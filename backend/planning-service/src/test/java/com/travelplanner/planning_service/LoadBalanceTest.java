package com.travelplanner.planning_service;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class LoadBalanceTest {

    @Autowired
    private RestTemplate restTemplate; // @LoadBalanced - ide kroz Eureku

    @Autowired
    @Qualifier("directTemplate")
    private RestTemplate directRestTemplate; // direktno na 8082

    private static final String LB_URL     = "http://PLANNING-SERVICE/api/travel-plans/lb-test/";
    private static final String DIRECT_URL = "http://localhost:8082/api/travel-plans/lb-test/";
    private static final int TOTAL_REQUESTS = 100;
    private static final int THREADS        = 50;
    private static final int MAX_ID         = 10;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void testLoadBalancing() throws InterruptedException {

        // SA LOAD BALANCINGOM
        AtomicInteger count8082 = new AtomicInteger(0);
        AtomicInteger count8083 = new AtomicInteger(0);
        AtomicInteger lbErrors  = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        CountDownLatch latch = new CountDownLatch(TOTAL_REQUESTS);

        long startLB = System.currentTimeMillis();

        for (int i = 0; i < TOTAL_REQUESTS; i++) {
            executor.submit(() -> {
                try {
                    int id = new Random().nextInt(MAX_ID) + 1;
                    ResponseEntity<String> response = restTemplate
                        .getForEntity(LB_URL + id, String.class);
                    
                    JsonNode json = mapper.readTree(response.getBody());
                    int port = json.get("port").asInt();

                    if (port == 8082) count8082.incrementAndGet();
                    if (port == 8083) count8083.incrementAndGet();
                } catch (Exception e) {
                    lbErrors.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(60, TimeUnit.SECONDS);
        long durationLB = System.currentTimeMillis() - startLB;
        executor.shutdown();

        System.out.println("\nSA LOAD BALANCINGOM");
        System.out.printf( "8082 : %3d zahtjeva          %n", count8082.get());
        System.out.printf( "8083 : %3d zahtjeva          %n", count8083.get());
        System.out.printf( "Ukupno vrijeme : %4d ms                %n", durationLB);

        Thread.sleep(2000);

        // BEZ LOAD BALANCINGA
        AtomicInteger directCount  = new AtomicInteger(0);
        AtomicInteger directErrors = new AtomicInteger(0);

        ExecutorService executor2 = Executors.newFixedThreadPool(THREADS);
        CountDownLatch latch2 = new CountDownLatch(TOTAL_REQUESTS);

        long startDirect = System.currentTimeMillis();

        for (int i = 0; i < TOTAL_REQUESTS; i++) {
            executor2.submit(() -> {
                try {
                    int id = new Random().nextInt(MAX_ID) + 1;
                    ResponseEntity<String> response = directRestTemplate
                        .getForEntity(DIRECT_URL + id, String.class);
                    
                    JsonNode json = mapper.readTree(response.getBody());
                    int port = json.get("port").asInt();

                    if (port == 8082) directCount.incrementAndGet();
                } catch (Exception e) {
                    directErrors.incrementAndGet();
                } finally {
                    latch2.countDown();
                }
            });
        }

        latch2.await(60, TimeUnit.SECONDS);
        long durationDirect = System.currentTimeMillis() - startDirect;
        executor2.shutdown();

        System.out.println("\nBEZ LOAD BALANCINGA");
        System.out.printf( " 8082 : %3d zahtjeva   %n", directCount.get());
        System.out.printf( " Ukupno vrijeme : %4d ms         %n", durationDirect);
    }
}
