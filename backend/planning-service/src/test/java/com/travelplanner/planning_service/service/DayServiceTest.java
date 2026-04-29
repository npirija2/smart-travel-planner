package com.travelplanner.planning_service.service;

import com.travelplanner.planning_service.dto.DayRequestDTO;
import com.travelplanner.planning_service.dto.DayResponseDTO;
import com.travelplanner.planning_service.exception.ResourceNotFoundException;
import com.travelplanner.planning_service.model.Day;
import com.travelplanner.planning_service.model.TravelPlan;
import com.travelplanner.planning_service.repository.DayRepository;
import com.travelplanner.planning_service.repository.TravelPlanRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DayServiceTest {

    @Mock
    private DayRepository dayRepository;

    @Mock
    private TravelPlanRepository travelPlanRepository;

    @InjectMocks
    private DayService service;

    @Test
    void shouldCreateDayAndReturnMappedDTO() {
        DayRequestDTO dto = new DayRequestDTO();
        dto.setTravelPlanId(1L);
        dto.setDate(LocalDate.of(2025, 6, 15));

        TravelPlan plan = TravelPlan.builder().id(1L).build();
        Day saved = Day.builder()
                .id(10L)
                .date(LocalDate.of(2025, 6, 15))
                .travelPlan(plan)
                .build();

        when(travelPlanRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(dayRepository.save(any(Day.class))).thenReturn(saved);

        DayResponseDTO result = service.create(dto);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getDate()).isEqualTo(LocalDate.of(2025, 6, 15));
        assertThat(result.getTravelPlanId()).isEqualTo(1L);
    }

    @Test
    void shouldSaveDayWithCorrectFields() {
        DayRequestDTO dto = new DayRequestDTO();
        dto.setTravelPlanId(1L);
        dto.setDate(LocalDate.of(2025, 7, 20));

        TravelPlan plan = TravelPlan.builder().id(1L).build();
        Day saved = Day.builder().id(1L).date(dto.getDate()).travelPlan(plan).build();

        when(travelPlanRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(dayRepository.save(any(Day.class))).thenReturn(saved);

        service.create(dto);

        ArgumentCaptor<Day> captor = ArgumentCaptor.forClass(Day.class);
        verify(dayRepository).save(captor.capture());

        Day captured = captor.getValue();
        assertThat(captured.getDate()).isEqualTo(LocalDate.of(2025, 7, 20));
        assertThat(captured.getTravelPlan()).isEqualTo(plan);
    }

    @Test
    void shouldThrowWhenTravelPlanNotFound() {
        DayRequestDTO dto = new DayRequestDTO();
        dto.setTravelPlanId(99L);
        dto.setDate(LocalDate.of(2025, 6, 15));

        when(travelPlanRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("TravelPlan not found");

        verifyNoInteractions(dayRepository);
    }

    @Test
    void shouldReturnAllDaysMapped() {
        TravelPlan plan = TravelPlan.builder().id(1L).build();

        List<Day> days = List.of(
                Day.builder().id(1L).date(LocalDate.of(2025, 6, 1)).travelPlan(plan).build(),
                Day.builder().id(2L).date(LocalDate.of(2025, 6, 2)).travelPlan(plan).build()
        );
        when(dayRepository.findAll()).thenReturn(days);

        List<DayResponseDTO> result = service.getAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getDate()).isEqualTo(LocalDate.of(2025, 6, 1));
        assertThat(result.get(0).getTravelPlanId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getDate()).isEqualTo(LocalDate.of(2025, 6, 2));
    }

    @Test
    void shouldReturnEmptyListWhenNoDays() {
        when(dayRepository.findAll()).thenReturn(List.of());

        List<DayResponseDTO> result = service.getAll();

        assertThat(result).isEmpty();
    }
}