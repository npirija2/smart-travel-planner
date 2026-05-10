package com.travelplanner.user_service.service;

import com.travelplanner.user_service.dto.UserRequestDTO;
import com.travelplanner.user_service.dto.UserResponseDTO;
import com.travelplanner.user_service.exception.ResourceNotFoundException;
import com.travelplanner.user_service.mapper.UserMapper;
import com.travelplanner.user_service.model.User;
import com.travelplanner.user_service.repository.UserRepository;
import com.travelplanner.user_service.util.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

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
        when(passwordEncoder.encode(requestDTO.getPassword())).thenReturn("encoded-password");
        when(userRepository.save(any())).thenReturn(user);
        when(userMapper.toDto(any())).thenReturn(UserResponseDTO.builder().username("nejra").email("nejra@test.com").build());

        UserResponseDTO response = userService.createUser(requestDTO);

        assertNotNull(response);
        assertEquals("nejra", response.getUsername());
        verify(userRepository, times(1)).save(any());
    }

    @Test
    void testUpdateUser_Success() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);
        when(userMapper.toDto(any())).thenReturn(UserResponseDTO.builder().username("updated").email("updated@test.com").build());

        UserRequestDTO updateRequest = new UserRequestDTO();
        updateRequest.setUsername("updated");
        updateRequest.setEmail("updated@test.com");
        updateRequest.setPassword("newpassword123");

        UserResponseDTO response = userService.updateUser(1, updateRequest);

        assertNotNull(response);
        assertEquals("updated", response.getUsername());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testDeleteUser_Success() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        userService.deleteUser(1);

        verify(userRepository, times(1)).delete(user);
    }

    @Test
    void testGetUserById_NotFound() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(1));
    }
}
