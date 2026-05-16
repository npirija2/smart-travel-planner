package com.travelplanner.communication_service.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.travelplanner.communication_service.dto.VoteRequestDTO;
import com.travelplanner.communication_service.dto.VoteResponseDTO;
import com.travelplanner.communication_service.exception.ResourceNotFoundException; 
import com.travelplanner.communication_service.model.Vote;
import com.travelplanner.communication_service.repository.VoteRepository;
import com.travelplanner.communication_service.util.JwtUtils;

@Service
public class VoteService {

    private final VoteRepository voteRepository;
    private final JwtUtils jwtUtils; 

    public VoteService(VoteRepository voteRepository, JwtUtils jwtUtils) {
        this.voteRepository = voteRepository;
        this.jwtUtils = jwtUtils;
    }

    public VoteResponseDTO createVote(VoteRequestDTO requestDTO, String authHeader) {
        validateToken(authHeader);

        boolean alreadyExists = voteRepository.existsByUserIdAndActivityId(
                requestDTO.getUserId(),
                requestDTO.getActivityId()
        );

        if (alreadyExists) {
            throw new IllegalArgumentException("User has already voted for this activity");
        }

        Vote vote = new Vote();
        vote.setUserId(requestDTO.getUserId());
        vote.setActivityId(requestDTO.getActivityId());

        Vote saved = voteRepository.save(vote);
        return mapToResponseDTO(saved);
    }

    public List<VoteResponseDTO> getAllVotes(String authHeader) {
        validateToken(authHeader);
        return voteRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public VoteResponseDTO getVoteById(Integer id, String authHeader) {
        validateToken(authHeader); 
        Vote vote = voteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vote not found with id " + id));
        return mapToResponseDTO(vote);
    }

    public List<VoteResponseDTO> getVotesByUserId(Integer userId, String authHeader) {
        validateToken(authHeader); 
        return voteRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<VoteResponseDTO> getVotesByActivityId(Integer activityId, String authHeader) {
        validateToken(authHeader); 
        return voteRepository.findByActivityId(activityId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public void deleteVote(Integer id, String authHeader) {
        validateToken(authHeader);
        Vote vote = voteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vote not found with id " + id));
        voteRepository.delete(vote);
    }

    private void validateToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        jwtUtils.getClaims(token);
    }

    private VoteResponseDTO mapToResponseDTO(Vote vote) {
        VoteResponseDTO DTO = new VoteResponseDTO();
        DTO.setId(vote.getId());
        DTO.setUserId(vote.getUserId());
        DTO.setActivityId(vote.getActivityId());
        return DTO;
    }
}