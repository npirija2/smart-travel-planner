package com.travelplanner.communication_service.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.travelplanner.communication_service.dto.VoteRequestDTO;
import com.travelplanner.communication_service.dto.VoteResponseDTO;
import com.travelplanner.communication_service.exception.ResourceNotFoundException;
import com.travelplanner.communication_service.model.Vote;
import com.travelplanner.communication_service.repository.VoteRepository;
import com.travelplanner.shared.security.JwtValidator;

@ExtendWith(MockitoExtension.class)
class VoteServiceTest {

    private static final String AUTH_HEADER = "Bearer test-token";

    @Mock
    private VoteRepository voteRepository;

    @Mock
    private JwtValidator jwtUtils;

    @InjectMocks
    private VoteService voteService;

    private Vote vote;
    private VoteRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        vote = new Vote();
        vote.setId(1);
        vote.setUserId(10);
        vote.setActivityId(50);

        requestDTO = new VoteRequestDTO();
        requestDTO.setUserId(10);
        requestDTO.setActivityId(50);
    }

    @Test
    void shouldCreateVoteSuccessfully() {
        // Simuliramo da glas još ne postoji
        when(voteRepository.existsByUserIdAndActivityId(10, 50)).thenReturn(false);
        when(voteRepository.save(any(Vote.class))).thenReturn(vote);

        VoteResponseDTO response = voteService.createVote(requestDTO, AUTH_HEADER);

        assertNotNull(response);
        assertEquals(10, response.getUserId());
        assertEquals(50, response.getActivityId());
        verify(voteRepository, times(1)).save(any(Vote.class));
    }

    @Test
    void shouldThrowExceptionWhenUserAlreadyVoted() {
        // Simuliramo da glas VEĆ postoji
        when(voteRepository.existsByUserIdAndActivityId(10, 50)).thenReturn(true);

        // Provjeravamo baca li metodu ispravan Exception
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            voteService.createVote(requestDTO, AUTH_HEADER);
        });

        assertEquals("User has already voted for this activity", exception.getMessage());
        // Potvrđujemo da se save() nikada nije pozvao
        verify(voteRepository, never()).save(any(Vote.class));
    }

    @Test
    void shouldReturnAllVotes() {
        when(voteRepository.findAll()).thenReturn(List.of(vote));

        List<VoteResponseDTO> result = voteService.getAllVotes(AUTH_HEADER);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void shouldGetVoteById() {
        when(voteRepository.findById(1)).thenReturn(Optional.of(vote));

        VoteResponseDTO response = voteService.getVoteById(1, AUTH_HEADER);

        assertNotNull(response);
        assertEquals(1, response.getId());
    }

    @Test
    void shouldThrowNotFoundWhenVoteDoesNotExist() {
        when(voteRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            voteService.getVoteById(99, AUTH_HEADER);
        });
    }

    @Test
    void shouldGetVotesByUserId() {
        when(voteRepository.findByUserId(10)).thenReturn(List.of(vote));

        List<VoteResponseDTO> result = voteService.getVotesByUserId(10, AUTH_HEADER);

        assertEquals(1, result.size());
        assertEquals(10, result.get(0).getUserId());
    }

    @Test
    void shouldDeleteVoteSuccessfully() {
        when(voteRepository.findById(1)).thenReturn(Optional.of(vote));
        doNothing().when(voteRepository).delete(vote);

        voteService.deleteVote(1, AUTH_HEADER);

        verify(voteRepository, times(1)).delete(vote);
    }
}
