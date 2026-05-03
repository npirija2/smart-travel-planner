package com.travelplanner.communication_service.controller;

import com.travelplanner.communication_service.dto.SharedLinkRequestDTO;
import com.travelplanner.communication_service.dto.SharedLinkResponseDTO;
import com.travelplanner.communication_service.service.SharedLinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Shared Links", description = "Endpoints for managing shared links")
@RestController
@RequestMapping("/api/shared-links")
public class SharedLinkController {

    private final SharedLinkService sharedLinkService;

    public SharedLinkController(SharedLinkService sharedLinkService) {
        this.sharedLinkService = sharedLinkService;
    }

    @Operation(summary = "Create shared link")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Shared link created"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    public ResponseEntity<SharedLinkResponseDTO> createSharedLink(
            @Valid @RequestBody SharedLinkRequestDTO requestDTO,
            @RequestHeader("Authorization") String authHeader) { // DODANO
        return new ResponseEntity<>(sharedLinkService.createSharedLink(requestDTO, authHeader), HttpStatus.CREATED);
    }

    @Operation(summary = "Get all shared links")
    @GetMapping
    public ResponseEntity<List<SharedLinkResponseDTO>> getAllSharedLinks(
            @RequestHeader("Authorization") String authHeader) { // DODANO
        return ResponseEntity.ok(sharedLinkService.getAllSharedLinks(authHeader));
    }

    @Operation(summary = "Get shared link by id")
    @GetMapping("/{id}")
    public ResponseEntity<SharedLinkResponseDTO> getSharedLinkById(
            @PathVariable Integer id,
            @RequestHeader("Authorization") String authHeader) { // DODANO
        return ResponseEntity.ok(sharedLinkService.getSharedLinkById(id, authHeader));
    }

    @Operation(summary = "Get shared links by plan id")
    @GetMapping("/plan/{planId}")
    public ResponseEntity<List<SharedLinkResponseDTO>> getSharedLinksByPlanId(
            @PathVariable Integer planId,
            @RequestHeader("Authorization") String authHeader) { // DODANO
        return ResponseEntity.ok(sharedLinkService.getSharedLinksByPlanId(planId, authHeader));
    }

    @Operation(summary = "Update shared link")
    @PutMapping("/{id}")
    public ResponseEntity<SharedLinkResponseDTO> updateSharedLink(
            @PathVariable Integer id,
            @Valid @RequestBody SharedLinkRequestDTO requestDTO,
            @RequestHeader("Authorization") String authHeader) { // DODANO
        return ResponseEntity.ok(sharedLinkService.updateSharedLink(id, requestDTO, authHeader));
    }

    @Operation(summary = "Delete shared link")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSharedLink(
            @PathVariable Integer id,
            @RequestHeader("Authorization") String authHeader) { // DODANO
        sharedLinkService.deleteSharedLink(id, authHeader);
        return ResponseEntity.noContent().build();
    }
}