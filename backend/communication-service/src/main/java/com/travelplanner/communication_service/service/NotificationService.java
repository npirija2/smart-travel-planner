package com.travelplanner.communication_service.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.travelplanner.communication_service.dto.NotificationRequestDTO;
import com.travelplanner.communication_service.dto.NotificationResponseDTO;
import com.travelplanner.communication_service.exception.ResourceNotFoundException;
import com.travelplanner.communication_service.exception.UnauthorizedException;
import com.travelplanner.communication_service.model.Notification;
import com.travelplanner.communication_service.repository.NotificationRepository;
import com.travelplanner.shared.security.JwtValidator;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final JwtValidator jwtUtils;

    public NotificationService(NotificationRepository notificationRepository, JwtValidator jwtUtils) {
        this.notificationRepository = notificationRepository;
        this.jwtUtils = jwtUtils;
    }

    public NotificationResponseDTO createNotification(NotificationRequestDTO requestDTO, String authHeader) {
        validateToken(authHeader); // Provjera validnosti na ulazu
        
        Notification notification = mapToEntity(requestDTO);
        Notification saved = notificationRepository.save(notification);
        return mapToResponseDTO(saved);
    }

    @Transactional
    public List<NotificationResponseDTO> createNotifications(List<NotificationRequestDTO> requests, String authHeader) {
        validateToken(authHeader);
        
        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("notifications list must not be empty");
        }

        List<Notification> notifications = requests.stream()
                .map(this::mapToEntity)
                .collect(Collectors.toList());

        return notificationRepository.saveAll(notifications).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<NotificationResponseDTO> getAllNotifications(String authHeader) {
        validateToken(authHeader);
        return notificationRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public NotificationResponseDTO getNotificationById(int id, String authHeader) {
        validateToken(authHeader);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id " + id));
        return mapToResponseDTO(notification);
    }

    public List<NotificationResponseDTO> getNotificationsByUserId(int userId, String authHeader) {
        validateToken(authHeader);
        // Ovdje možeš dodati provjeru: da li je userId iz tokena isti kao ovaj iz putanje?
        return notificationRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<NotificationResponseDTO> getNotificationsByPlanId(int planId, String authHeader) {
        validateToken(authHeader);
        return notificationRepository.findByPlanId(planId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public NotificationResponseDTO updateNotification(int id, NotificationRequestDTO requestDTO, String authHeader) {
        validateToken(authHeader);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id " + id));

        notification.setMessage(requestDTO.getMessage());
        notification.setDate(requestDTO.getDate());
        notification.setUserId(requestDTO.getUserId());
        notification.setPlanId(requestDTO.getPlanId());
        notification.setType(requestDTO.getType());

        Notification updated = notificationRepository.save(notification);
        return mapToResponseDTO(updated);
    }

    public void deleteNotification(int id, String authHeader) {
        validateToken(authHeader);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id " + id));
        notificationRepository.delete(notification);
    }

    private void validateToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException(
                    "Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        try {
            jwtUtils.getClaims(token);
        }catch (Exception e) {

    e.printStackTrace();

    throw new UnauthorizedException(
            "Invalid or expired token");
}
    }

    private Notification mapToEntity(NotificationRequestDTO requestDTO) {
        Notification notification = new Notification();
        notification.setMessage(requestDTO.getMessage());
        notification.setDate(requestDTO.getDate());
        notification.setUserId(requestDTO.getUserId());
        notification.setPlanId(requestDTO.getPlanId());
        notification.setType(requestDTO.getType());
        return notification;
    }

    private NotificationResponseDTO mapToResponseDTO(Notification notification) {
        NotificationResponseDTO DTO = new NotificationResponseDTO();
        DTO.setId(notification.getId());
        DTO.setMessage(notification.getMessage());
        DTO.setDate(notification.getDate());
        DTO.setUserId(notification.getUserId());
        DTO.setPlanId(notification.getPlanId());
        DTO.setType(notification.getType());
        return DTO;
    }
}