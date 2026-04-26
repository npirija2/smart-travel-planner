package com.travelplanner.user_service.mapper;

import com.travelplanner.user_service.dto.PlanUserRequestDTO;
import com.travelplanner.user_service.dto.PlanUserResponseDTO;
import com.travelplanner.user_service.model.PlanUser;
import org.springframework.stereotype.Component;

@Component
public class PlanUserMapper {

    public PlanUserResponseDTO toResponseDTO(PlanUser planUser) {
        if (planUser == null) {
            return null;
        }

        PlanUserResponseDTO dto = new PlanUserResponseDTO();
        dto.setId(planUser.getId());
        dto.setUserId(planUser.getUser() != null ? planUser.getUser().getId() : null);
        dto.setPlanId(planUser.getPlanId());
        dto.setRole(planUser.getRole());
        return dto;
    }

    public PlanUser toEntity(PlanUserRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        return PlanUser.builder()
                .planId(dto.getPlanId())
                .role(dto.getRole())
                .build();
    }

    public void updateEntityFromDTO(PlanUserRequestDTO dto, PlanUser planUser) {
        if (dto == null || planUser == null) {
            return;
        }

        planUser.setPlanId(dto.getPlanId());
        planUser.setRole(dto.getRole());
    }
}
