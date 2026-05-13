package com.travelplanner.user_service.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.travelplanner.user_service.dto.UserRequestDTO;
import com.travelplanner.user_service.dto.UserResponseDTO;
import com.travelplanner.user_service.exception.ResourceNotFoundException;
import com.travelplanner.user_service.mapper.UserMapper;
import com.travelplanner.user_service.model.User;
import com.travelplanner.user_service.repository.UserRepository;
import com.travelplanner.user_service.util.JwtUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("Pogrešna lozinka");
        }
        return jwtUtils.generateToken(user);
    }

    @Transactional
    public UserResponseDTO createUser(UserRequestDTO request) {
        User user = userMapper.toEntity(request);
        
        // Heširaj lozinku prije spašavanja
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        
        // Ako u bazi role ne smije biti null, postavi default ako ga nema u requestu
        if (user.getRole() == null) {
            user.setRole("ROLE_USER");
        }

        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Korisnik sa ID-jem " + id + " nije pronađen"));
        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponseDTO updateUser(Integer id, UserRequestDTO request) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Korisnik sa ID-jem " + id + " nije pronađen"));

        existingUser.setUsername(request.getUsername());
        existingUser.setEmail(request.getEmail());
        existingUser.setPasswordHash(request.getPassword());

        User savedUser = userRepository.save(existingUser);
        return userMapper.toDto(savedUser);
    }

    @Transactional
    public void deleteUser(Integer id) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Korisnik sa ID-jem " + id + " nije pronađen"));

        userRepository.delete(existingUser);
    }
}
