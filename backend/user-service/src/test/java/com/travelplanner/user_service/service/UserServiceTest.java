package com.travelplanner.user_service.service;

import com.travelplanner.user_service.dto.UserRequestDTO;
import com.travelplanner.user_service.dto.UserResponseDTO;
import com.travelplanner.user_service.mapper.UserMapper;
import com.travelplanner.user_service.model.User;
import com.travelplanner.user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1).username("nejra").email("nejra@test.com").build();
        requestDTO = new UserRequestDTO();
        requestDTO.setUsername("nejra");
        requestDTO.setEmail("nejra@test.com");
        requestDTO.setPassword("password123");
    }

    @Test
    void testCreateUser_Success() {
        when(userMapper.toEntity(any())).thenReturn(user);
        when(userRepository.save(any())).thenReturn(user);
        when(userMapper.toDto(any())).thenReturn(UserResponseDTO.builder().username("nejra").email("nejra@test.com").build());

        UserResponseDTO response = userService.createUser(requestDTO);

        assertNotNull(response);
        assertEquals("nejra", response.getUsername());
        verify(userRepository, times(1)).save(any());
    }
}