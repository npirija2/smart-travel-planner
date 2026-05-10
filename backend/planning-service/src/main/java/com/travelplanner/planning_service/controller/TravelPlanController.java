package com.travelplanner.planning_service.controller;

import java.util.List;
import java.util.Map;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.travelplanner.planning_service.dto.TravelPlanRequestDTO;
import com.travelplanner.planning_service.dto.TravelPlanResponseDTO;
import com.travelplanner.planning_service.service.TravelPlanService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/travel-plans")
@RequiredArgsConstructor
public class TravelPlanController {

    private final TravelPlanService travelPlanService;

    @Value("${server.port}")
    private String port;

    // Glavna metoda za dobavljanje planova (filtrira po korisniku iz tokena)
    @GetMapping
    public ResponseEntity<List<TravelPlanResponseDTO>> getAll(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(travelPlanService.getAll(authHeader));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TravelPlanResponseDTO> getById(@PathVariable Long id, 
                                                         @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(travelPlanService.getById(id, authHeader));
    }

    @PostMapping
    public ResponseEntity<TravelPlanResponseDTO> create(@Valid @RequestBody TravelPlanRequestDTO dto,
                                                        @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.status(HttpStatus.CREATED).body(travelPlanService.create(dto, authHeader));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TravelPlanResponseDTO> update(@PathVariable Long id,
                                                        @Valid @RequestBody TravelPlanRequestDTO dto,
                                                        @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(travelPlanService.update(id, dto, authHeader));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @RequestHeader("Authorization") String authHeader) {
        travelPlanService.delete(id, authHeader);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<TravelPlanResponseDTO>> getPaged(Pageable pageable) {
        return ResponseEntity.ok(travelPlanService.getAllPaged(pageable));
    }

    @GetMapping("/user/{ownerId}")
    public ResponseEntity<List<TravelPlanResponseDTO>> getByOwner(@PathVariable Long ownerId) {
        return ResponseEntity.ok(travelPlanService.getByOwnerId(ownerId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<TravelPlanResponseDTO>> getByStatus(@PathVariable String status) {
        return ResponseEntity.ok(travelPlanService.getByStatus(status));
    }
    @GetMapping("/lb-test/{id}")
    public ResponseEntity<Map<String, Object>> lbTest(
            @PathVariable Long id,
            HttpServletRequest request) throws InterruptedException {
        
        Thread.sleep(200); // simulira kompleksan DB upit 
        
        boolean exists;
        try {
            travelPlanService.getById(id);
            exists = true;
        } catch (Exception e) {
            exists = false;
        }
        
        return ResponseEntity.ok(Map.of(
            "port", request.getLocalPort(),
            "id", id,
            "exists", exists
        ));
    }
}