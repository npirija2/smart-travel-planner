package com.travelplanner.communication_service.controller;

import com.travelplanner.communication_service.dto.VoteRequestDTO;
import com.travelplanner.communication_service.dto.VoteResponseDTO;
import com.travelplanner.communication_service.service.VoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Votes", description = "Endpoints for managing votes")
@RestController
@RequestMapping("/api/votes")
public class VoteController {

    private final VoteService voteService;

    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }

    @Operation(summary = "Create vote")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Vote created"),
            @ApiResponse(responseCode = "400", description = "Invalid input or duplicate vote")
    })
    @PostMapping
    public ResponseEntity<VoteResponseDTO> createVote(
            @Valid @RequestBody VoteRequestDTO requestDTO,
            @RequestHeader("Authorization") String authHeader) { // DODANO
        return new ResponseEntity<>(voteService.createVote(requestDTO, authHeader), HttpStatus.CREATED);
    }

    @Operation(summary = "Get all votes")
    @GetMapping
    public ResponseEntity<List<VoteResponseDTO>> getAllVotes(
            @RequestHeader("Authorization") String authHeader) { // DODANO
        return ResponseEntity.ok(voteService.getAllVotes(authHeader));
    }

    @Operation(summary = "Get vote by id")
    @GetMapping("/{id}")
    public ResponseEntity<VoteResponseDTO> getVoteById(
            @PathVariable Integer id,
            @RequestHeader("Authorization") String authHeader) { // DODANO
        return ResponseEntity.ok(voteService.getVoteById(id, authHeader));
    }

    @Operation(summary = "Get votes by user id")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<VoteResponseDTO>> getVotesByUserId(
            @PathVariable Integer userId,
            @RequestHeader("Authorization") String authHeader) { // DODANO
        return ResponseEntity.ok(voteService.getVotesByUserId(userId, authHeader));
    }

    @Operation(summary = "Get votes by activity id")
    @GetMapping("/activity/{activityId}")
    public ResponseEntity<List<VoteResponseDTO>> getVotesByActivityId(
            @PathVariable Integer activityId,
            @RequestHeader("Authorization") String authHeader) { // DODANO
        return ResponseEntity.ok(voteService.getVotesByActivityId(activityId, authHeader));
    }

    @Operation(summary = "Delete vote")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVote(
            @PathVariable Integer id,
            @RequestHeader("Authorization") String authHeader) { // DODANO
        voteService.deleteVote(id, authHeader);
        return ResponseEntity.noContent().build();
    }
}