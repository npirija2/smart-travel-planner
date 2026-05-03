package com.travelplanner.user_service.mapper;

import org.springframework.stereotype.Component;

import com.travelplanner.user_service.dto.UserRequestDTO;
import com.travelplanner.user_service.dto.UserResponseDTO;
import com.travelplanner.user_service.model.User;

@Component
public class UserMapper {

    public User toEntity(UserRequestDTO dto) {
        if (dto == null) return null;

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        // Ovako mapiramo password iz DTO u entitet
        user.setPasswordHash(dto.getPassword());
        user.setRole("ROLE_USER");
        
        return user;
    }

    public UserResponseDTO toDto(User entity) {
        if (entity == null) return null;

        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(entity.getId());
        dto.setUsername(entity.getUsername());
        dto.setEmail(entity.getEmail());
        // Ako UserResponseDTO ima polje role, dodaj i njega ovdje
        return dto;
    }
}