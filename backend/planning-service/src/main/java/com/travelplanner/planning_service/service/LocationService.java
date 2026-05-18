package com.travelplanner.planning_service.service;

import com.travelplanner.planning_service.dto.LocationRequestDTO;
import com.travelplanner.planning_service.dto.LocationResponseDTO;
import com.travelplanner.planning_service.exception.ResourceNotFoundException;
import com.travelplanner.planning_service.model.Destination;
import com.travelplanner.planning_service.model.Location;
import com.travelplanner.planning_service.repository.DestinationRepository;
import com.travelplanner.planning_service.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;
    private final DestinationRepository destinationRepository;

    public List<LocationResponseDTO> getAll() {
        return locationRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public LocationResponseDTO getById(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location with id " + id + " not found"));
        return mapToResponseDTO(location);
    }

    public List<LocationResponseDTO> getByDestinationId(Long destinationId) {
        return locationRepository.findByDestinationId(destinationId).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public LocationResponseDTO create(LocationRequestDTO dto) {
        Destination destination = destinationRepository.findById(dto.getDestinationId())
                .orElseThrow(() -> new ResourceNotFoundException("Destination with id " + dto.getDestinationId() + " not found"));

        Location location = Location.builder()
                .name(dto.getName())
                .destination(destination)
                .address(dto.getAddress())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .type(dto.getType())
                .build();

        return mapToResponseDTO(locationRepository.save(location));
    }

    public LocationResponseDTO update(Long id, LocationRequestDTO dto) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location with id " + id + " not found"));

        Destination destination = destinationRepository.findById(dto.getDestinationId())
                .orElseThrow(() -> new ResourceNotFoundException("Destination with id " + dto.getDestinationId() + " not found"));

        location.setName(dto.getName());
        location.setDestination(destination);
        location.setAddress(dto.getAddress());
        location.setLatitude(dto.getLatitude());
        location.setLongitude(dto.getLongitude());
        location.setType(dto.getType());

        return mapToResponseDTO(locationRepository.save(location));
    }

    public void delete(Long id) {
        if (!locationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Location with id " + id + " not found");
        }
        locationRepository.deleteById(id);
    }

    private LocationResponseDTO mapToResponseDTO(Location location) {
        return LocationResponseDTO.builder()
                .id(location.getId())
                .name(location.getName())
                .destinationId(location.getDestination().getId())
                .destinationName(location.getDestination().getName())
                .address(location.getAddress())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .type(location.getType())
                .build();
    }
}
