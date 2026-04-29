package com.travelplanner.planning_service.service;

import com.travelplanner.planning_service.dto.DestinationRequestDTO;
import com.travelplanner.planning_service.dto.DestinationResponseDTO;
import com.travelplanner.planning_service.exception.ResourceNotFoundException;
import com.travelplanner.planning_service.model.Destination;
import com.travelplanner.planning_service.repository.DestinationRepository;

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
class DestinationServiceTest {

    @Mock
    private DestinationRepository repository;

    @InjectMocks
    private DestinationService service;

    @Test
    void shouldReturnAllDestinationsMapped() {
        List<Destination> destinations = List.of(
                Destination.builder().id(1L).name("Paris").build(),
                Destination.builder().id(2L).name("Rome").build()
        );
        when(repository.findAll()).thenReturn(destinations);

        List<DestinationResponseDTO> result = service.getAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getName()).isEqualTo("Paris");
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getName()).isEqualTo("Rome");
    }

    @Test
    void shouldReturnEmptyListWhenNoDestinations() {
        when(repository.findAll()).thenReturn(List.of());

        List<DestinationResponseDTO> result = service.getAll();

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnDestinationById() {
        // given
        Destination destination = Destination.builder().id(1L).name("Paris").build();
        when(repository.findById(1L)).thenReturn(Optional.of(destination));

        DestinationResponseDTO result = service.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Paris");
    }

    @Test
    void shouldThrowWhenDestinationNotFoundById() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Destination not found");
    }

    @Test
    void shouldCreateDestinationAndReturnMappedDTO() {
        DestinationRequestDTO dto = new DestinationRequestDTO();
        dto.setName("Tokyo");

        Destination saved = Destination.builder().id(3L).name("Tokyo").build();
        when(repository.save(any(Destination.class))).thenReturn(saved);

        DestinationResponseDTO result = service.create(dto);

        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getName()).isEqualTo("Tokyo");
    }

    @Test
    void shouldSaveDestinationWithCorrectName() {
        DestinationRequestDTO dto = new DestinationRequestDTO();
        dto.setName("Berlin");

        Destination saved = Destination.builder().id(4L).name("Berlin").build();
        when(repository.save(any(Destination.class))).thenReturn(saved);

        service.create(dto);

        ArgumentCaptor<Destination> captor = ArgumentCaptor.forClass(Destination.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Berlin");
    }

    @Test
    void shouldCallDeleteByIdWithCorrectId() {
        service.delete(5L);

        verify(repository).deleteById(5L);
    }
}