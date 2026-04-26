package com.travelplanner.communication_service.service;

import com.travelplanner.communication_service.dto.NotificationRequestDTO;
import com.travelplanner.communication_service.dto.NotificationResponseDTO;
import com.travelplanner.communication_service.exception.ResourceNotFoundException;
import com.travelplanner.communication_service.model.Notification;
import com.travelplanner.communication_service.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public NotificationResponseDTO createNotification(NotificationRequestDTO requestDTO) {
        Notification notification = new Notification();
        notification.setMessage(requestDTO.getMessage());
        notification.setDate(requestDTO.getDate());
        notification.setUserId(requestDTO.getUserId());
        notification.setPlanId(requestDTO.getPlanId());
        notification.setType(requestDTO.getType());

        Notification saved = notificationRepository.save(notification);

        NotificationResponseDTO response = new NotificationResponseDTO();
        response.setId(saved.getId());
        response.setMessage(saved.getMessage());
        response.setDate(saved.getDate());
        response.setUserId(saved.getUserId());
        response.setPlanId(saved.getPlanId());
        response.setType(saved.getType());

        return response;
    }
    public List<NotificationResponseDTO> getAllNotifications() {
        return notificationRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public NotificationResponseDTO getNotificationById(int id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id " + id));
        return mapToResponseDTO(notification);
    }

    public List<NotificationResponseDTO> getNotificationsByUserId(int userId) {
        return notificationRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<NotificationResponseDTO> getNotificationsByPlanId(int planId) {
        return notificationRepository.findByPlanId(planId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public NotificationResponseDTO updateNotification(int id, NotificationRequestDTO requestDTO) {
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

    public void deleteNotification(int id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id " + id));
        notificationRepository.delete(notification);
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
