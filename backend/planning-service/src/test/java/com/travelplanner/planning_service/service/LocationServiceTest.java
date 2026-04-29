package com.travelplanner.planning_service.service;

import com.travelplanner.planning_service.dto.LocationRequestDTO;
import com.travelplanner.planning_service.dto.LocationResponseDTO;
import com.travelplanner.planning_service.exception.ResourceNotFoundException;
import com.travelplanner.planning_service.model.Destination;
import com.travelplanner.planning_service.model.Location;
import com.travelplanner.planning_service.repository.DestinationRepository;
import com.travelplanner.planning_service.repository.LocationRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private DestinationRepository destinationRepository;

    @InjectMocks
    private LocationService service;

    private Destination buildDestination() {
        return Destination.builder().id(1L).name("Paris").build();
    }

    private Location buildLocation(Destination destination) {
        return Location.builder()
                .id(10L)
                .name("Eiffel Tower")
                .destination(destination)
                .address("Champ de Mars")
                .latitude(48.8584)
                .longitude(2.2945)
                .type("ATTRACTION")
                .build();
    }

    private LocationRequestDTO buildRequestDTO() {
        LocationRequestDTO dto = new LocationRequestDTO();
        dto.setDestinationId(1L);
        dto.setName("Eiffel Tower");
        dto.setAddress("Champ de Mars");
        dto.setLatitude(48.8584);
        dto.setLongitude(2.2945);
        dto.setType("ATTRACTION");
        return dto;
    }

    @Test
    void shouldReturnAllLocationsMapped() {
        Destination destination = buildDestination();
        List<Location> locations = List.of(
                buildLocation(destination),
                Location.builder().id(11L).name("Louvre").destination(destination)
                        .address("Rue de Rivoli").latitude(48.8606).longitude(2.3376).type("MUSEUM").build()
        );
        when(locationRepository.findAll()).thenReturn(locations);

        List<LocationResponseDTO> result = service.getAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(10L);
        assertThat(result.get(0).getName()).isEqualTo("Eiffel Tower");
        assertThat(result.get(0).getDestinationId()).isEqualTo(1L);
        assertThat(result.get(0).getDestinationName()).isEqualTo("Paris");
        assertThat(result.get(1).getId()).isEqualTo(11L);
        assertThat(result.get(1).getName()).isEqualTo("Louvre");
    }

    @Test
    void shouldReturnEmptyListWhenNoLocations() {
        when(locationRepository.findAll()).thenReturn(List.of());

        List<LocationResponseDTO> result = service.getAll();

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnLocationById() {
        Destination destination = buildDestination();
        Location location = buildLocation(destination);
        when(locationRepository.findById(10L)).thenReturn(Optional.of(location));

        LocationResponseDTO result = service.getById(10L);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("Eiffel Tower");
        assertThat(result.getDestinationId()).isEqualTo(1L);
        assertThat(result.getDestinationName()).isEqualTo("Paris");
        assertThat(result.getAddress()).isEqualTo("Champ de Mars");
        assertThat(result.getLatitude()).isEqualTo(48.8584);
        assertThat(result.getLongitude()).isEqualTo(2.2945);
        assertThat(result.getType()).isEqualTo("ATTRACTION");
    }

    @Test
    void shouldThrowWhenLocationNotFoundById() {
        when(locationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void shouldCreateLocationAndReturnMappedDTO() {
        Destination destination = buildDestination();
        LocationRequestDTO dto = buildRequestDTO();
        Location saved = buildLocation(destination);

        when(destinationRepository.findById(1L)).thenReturn(Optional.of(destination));
        when(locationRepository.save(any(Location.class))).thenReturn(saved);

        LocationResponseDTO result = service.create(dto);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("Eiffel Tower");
        assertThat(result.getDestinationId()).isEqualTo(1L);
        assertThat(result.getDestinationName()).isEqualTo("Paris");
    }

    @Test
    void shouldSaveLocationWithCorrectFields() {
        Destination destination = buildDestination();
        LocationRequestDTO dto = buildRequestDTO();
        Location saved = buildLocation(destination);

        when(destinationRepository.findById(1L)).thenReturn(Optional.of(destination));
        when(locationRepository.save(any(Location.class))).thenReturn(saved);

        service.create(dto);

        ArgumentCaptor<Location> captor = ArgumentCaptor.forClass(Location.class);
        verify(locationRepository).save(captor.capture());

        Location captured = captor.getValue();
        assertThat(captured.getName()).isEqualTo("Eiffel Tower");
        assertThat(captured.getDestination()).isEqualTo(destination);
        assertThat(captured.getAddress()).isEqualTo("Champ de Mars");
        assertThat(captured.getLatitude()).isEqualTo(48.8584);
        assertThat(captured.getLongitude()).isEqualTo(2.2945);
        assertThat(captured.getType()).isEqualTo("ATTRACTION");
    }

    @Test
    void shouldThrowWhenDestinationNotFoundOnCreate() {
        LocationRequestDTO dto = buildRequestDTO();
        when(destinationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("1");

        verifyNoInteractions(locationRepository);
    }

    @Test
    void shouldUpdateLocationAndReturnMappedDTO() {
        Destination destination = buildDestination();
        Location existing = buildLocation(destination);
        LocationRequestDTO dto = buildRequestDTO();
        dto.setName("Eiffel Tower Updated");

        Location updated = Location.builder()
                .id(10L).name("Eiffel Tower Updated").destination(destination)
                .address("Champ de Mars").latitude(48.8584).longitude(2.2945).type("ATTRACTION")
                .build();

        when(locationRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(destinationRepository.findById(1L)).thenReturn(Optional.of(destination));
        when(locationRepository.save(existing)).thenReturn(updated);

        LocationResponseDTO result = service.update(10L, dto);

        assertThat(result.getName()).isEqualTo("Eiffel Tower Updated");
        assertThat(result.getId()).isEqualTo(10L);
    }

    @Test
    void shouldThrowWhenLocationNotFoundOnUpdate() {
        LocationRequestDTO dto = buildRequestDTO();
        when(locationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verifyNoInteractions(destinationRepository);
    }

    @Test
    void shouldThrowWhenDestinationNotFoundOnUpdate() {
        Destination destination = buildDestination();
        Location existing = buildLocation(destination);
        LocationRequestDTO dto = buildRequestDTO();

        when(locationRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(destinationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(10L, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("1");

        verify(locationRepository, never()).save(any());
    }

    @Test
    void shouldDeleteLocationWhenExists() {
        when(locationRepository.existsById(10L)).thenReturn(true);

        service.delete(10L);

        verify(locationRepository).deleteById(10L);
    }

    @Test
    void shouldThrowWhenLocationNotFoundOnDelete() {
        when(locationRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(locationRepository, never()).deleteById(any());
    }
}