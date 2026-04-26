package com.travelplanner.user_service.service;

import com.travelplanner.user_service.dto.PlanUserRequestDTO;
import com.travelplanner.user_service.dto.PlanUserResponseDTO;
import com.travelplanner.user_service.exception.ResourceNotFoundException;
import com.travelplanner.user_service.mapper.PlanUserMapper;
import com.travelplanner.user_service.model.PlanUser;
import com.travelplanner.user_service.model.User;
import com.travelplanner.user_service.repository.PlanUserRepository;
import com.travelplanner.user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PlanUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PlanUserRepository planUserRepository;

    @Mock
    private PlanUserMapper planUserMapper;

    @InjectMocks
    private PlanUserService planUserService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1).username("nejra").email("nejra@test.com").build();
    }

    @Test
    void createPlanUser_success() {
        PlanUserRequestDTO request = new PlanUserRequestDTO();
        request.setPlanId(42);
        request.setRole("OWNER");

        PlanUser planUser = PlanUser.builder()
                .id(20)
                .user(user)
                .planId(42)
                .role("OWNER")
                .build();

        PlanUserResponseDTO mappedResponse = new PlanUserResponseDTO();
        mappedResponse.setId(20);
        mappedResponse.setUserId(1);
        mappedResponse.setPlanId(42);
        mappedResponse.setRole("OWNER");

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(planUserMapper.toEntity(request)).thenReturn(PlanUser.builder()
                .planId(42)
                .role("OWNER")
                .build());
        when(planUserRepository.save(any())).thenReturn(planUser);
        when(planUserMapper.toResponseDTO(any(PlanUser.class))).thenReturn(mappedResponse);

        PlanUserResponseDTO response = planUserService.createPlanUser(1, request);

        assertNotNull(response);
        assertEquals(20, response.getId());
        assertEquals(42, response.getPlanId());
    }

    @Test
    void updatePlanUser_success() {
        PlanUser existing = PlanUser.builder()
                .id(20)
                .user(user)
                .planId(42)
                .role("OWNER")
                .build();

        when(planUserRepository.findById(20)).thenReturn(Optional.of(existing));
        when(planUserRepository.save(any())).thenReturn(existing);

        PlanUserResponseDTO mappedResponse = new PlanUserResponseDTO();
        mappedResponse.setId(20);
        mappedResponse.setUserId(1);
        mappedResponse.setPlanId(55);
        mappedResponse.setRole("MEMBER");
        when(planUserMapper.toResponseDTO(any(PlanUser.class))).thenReturn(mappedResponse);

        PlanUserRequestDTO request = new PlanUserRequestDTO();
        request.setPlanId(55);
        request.setRole("MEMBER");

        PlanUserResponseDTO response = planUserService.updatePlanUser(20, request);

        assertNotNull(response);
        assertEquals(55, response.getPlanId());
        assertEquals("MEMBER", response.getRole());
    }

    @Test
    void deletePlanUser_success() {
        PlanUser existing = PlanUser.builder()
                .id(20)
                .user(user)
                .planId(42)
                .role("OWNER")
                .build();

        when(planUserRepository.findById(20)).thenReturn(Optional.of(existing));

        planUserService.deletePlanUser(20);

        verify(planUserRepository).delete(existing);
    }

    @Test
    void getPlanUserById_notFound() {
        when(planUserRepository.findById(20)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> planUserService.getPlanUserById(20));
    }
}
