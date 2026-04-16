package com.travelplanner.communication_service.service;

import com.travelplanner.communication_service.dto.VoteRequestDTO;
import com.travelplanner.communication_service.dto.VoteResponseDTO;
import com.travelplanner.communication_service.exception.ResourceNotFoundException;
import com.travelplanner.communication_service.model.Vote;
import com.travelplanner.communication_service.repository.VoteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VoteService {

    private final VoteRepository voteRepository;

    public VoteService(VoteRepository voteRepository) {
        this.voteRepository = voteRepository;
    }

    public VoteResponseDTO createVote(VoteRequestDTO requestDTO) {
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

    public List<VoteResponseDTO> getAllVotes() {
        return voteRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public VoteResponseDTO getVoteById(Integer id) {
        Vote vote = voteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vote not found with id " + id));
        return mapToResponseDTO(vote);
    }

    public List<VoteResponseDTO> getVotesByUserId(Integer userId) {
        return voteRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<VoteResponseDTO> getVotesByActivityId(Integer activityId) {
        return voteRepository.findByActivityId(activityId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public void deleteVote(Integer id) {
        Vote vote = voteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vote not found with id " + id));
        voteRepository.delete(vote);
    }

    private VoteResponseDTO mapToResponseDTO(Vote vote) {
        VoteResponseDTO DTO = new VoteResponseDTO();
        DTO.setId(vote.getId());
        DTO.setUserId(vote.getUserId());
        DTO.setActivityId(vote.getActivityId());
        return DTO;
    }
}