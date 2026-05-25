package com.travelplanner.planning_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.travelplanner.planning_service.dto.WeatherForecastResponseDTO;
import com.travelplanner.planning_service.exception.ResourceNotFoundException;
import com.travelplanner.planning_service.model.Destination;
import com.travelplanner.planning_service.model.Location;
import com.travelplanner.planning_service.model.TravelPlan;
import com.travelplanner.planning_service.repository.LocationRepository;

@ExtendWith(MockitoExtension.class)
class WeatherForecastServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private RestTemplate restTemplate;

    private WeatherForecastService weatherForecastService;

    @BeforeEach
    void setUp() {
        weatherForecastService = new WeatherForecastService(locationRepository, restTemplate);
        ReflectionTestUtils.setField(weatherForecastService, "forecastUrl", "https://api.open-meteo.com/v1/forecast");
        ReflectionTestUtils.setField(weatherForecastService, "geocodingUrl", "https://geocoding-api.open-meteo.com/v1/search");
        ReflectionTestUtils.setField(weatherForecastService, "maxForecastDays", 16);
    }

    @Test
    void getForecastForPlan_usesExistingLocationCoordinates() {
        TravelPlan plan = plan("Rome");
        LocalDate date = LocalDate.now().plusDays(3);

        when(locationRepository.findByDestinationId(2L)).thenReturn(List.of(
                Location.builder()
                        .id(1L)
                        .name("Colosseum")
                        .address("Rome")
                        .latitude(41.8902)
                        .longitude(12.4922)
                        .type("culture")
                        .destination(plan.getDestination())
                        .build()
        ));

        when(restTemplate.getForEntity(any(URI.class), eq(WeatherForecastService.ForecastResponse.class)))
                .thenReturn(ResponseEntity.ok(new WeatherForecastService.ForecastResponse(
                        new WeatherForecastService.DailyForecast(
                                List.of(date),
                                List.of(0),
                                List.of(23.6)
                        )
                )));

        List<WeatherForecastResponseDTO> result = weatherForecastService.getForecastForPlan(plan, List.of(date));

        assertEquals(1, result.size());
        assertEquals("Sunny", result.get(0).getCondition());
        assertEquals(24, result.get(0).getTemperatureCelsius());
        verify(restTemplate, never()).getForEntity(any(URI.class), eq(WeatherForecastService.GeocodingResponse.class));
    }

    @Test
    void getForecastForPlan_fallsBackToGeocodingWhenDestinationHasNoCoordinates() {
        TravelPlan plan = plan("Venice, Italy");
        LocalDate date = LocalDate.now().plusDays(4);

        when(locationRepository.findByDestinationId(2L)).thenReturn(List.of());
        when(restTemplate.getForEntity(any(URI.class), eq(WeatherForecastService.GeocodingResponse.class)))
                .thenReturn(ResponseEntity.ok(new WeatherForecastService.GeocodingResponse(
                        List.of(new WeatherForecastService.GeocodingResult(48.8566, 2.3522))
                )));
        when(restTemplate.getForEntity(any(URI.class), eq(WeatherForecastService.ForecastResponse.class)))
                .thenReturn(ResponseEntity.ok(new WeatherForecastService.ForecastResponse(
                        new WeatherForecastService.DailyForecast(
                                List.of(date),
                                List.of(61),
                                List.of(18.2)
                        )
                )));

        List<WeatherForecastResponseDTO> result = weatherForecastService.getForecastForPlan(plan, List.of(date));

        assertFalse(result.isEmpty());
        assertEquals("Rain", result.get(0).getCondition());
        assertEquals(18, result.get(0).getTemperatureCelsius());
    }

    @Test
    void getForecastForPlan_ignoresInvalidSavedCoordinatesAndFallsBackToGeocoding() {
        TravelPlan plan = plan("Berlin");
        LocalDate date = LocalDate.now().plusDays(2);

        when(locationRepository.findByDestinationId(2L)).thenReturn(List.of(
                Location.builder()
                        .id(1L)
                        .name("Broken location")
                        .address("Berlin")
                        .latitude(34302.668)
                        .longitude(18.413)
                        .type("test")
                        .destination(plan.getDestination())
                        .build()
        ));
        when(restTemplate.getForEntity(any(URI.class), eq(WeatherForecastService.GeocodingResponse.class)))
                .thenReturn(ResponseEntity.ok(new WeatherForecastService.GeocodingResponse(
                        List.of(new WeatherForecastService.GeocodingResult(52.52, 13.405))
                )));
        when(restTemplate.getForEntity(any(URI.class), eq(WeatherForecastService.ForecastResponse.class)))
                .thenReturn(ResponseEntity.ok(new WeatherForecastService.ForecastResponse(
                        new WeatherForecastService.DailyForecast(
                                List.of(date),
                                List.of(3),
                                List.of(20.1)
                        )
                )));

        List<WeatherForecastResponseDTO> result = weatherForecastService.getForecastForPlan(plan, List.of(date));

        assertEquals(1, result.size());
        assertEquals("Cloudy", result.get(0).getCondition());
    }

    @Test
    void getForecastForPlan_throwsFriendlyMessageWhenTripIsOutsideProviderRange() {
        TravelPlan plan = plan("Berlin");
        LocalDate farFutureDate = LocalDate.now().plusDays(25);

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> weatherForecastService.getForecastForPlan(plan, List.of(farFutureDate))
        );

        org.junit.jupiter.api.Assertions.assertTrue(exception.getMessage().contains("Weather forecast is not available yet"));
        verify(locationRepository, never()).findByDestinationId(any());
        verify(restTemplate, never()).getForEntity(any(URI.class), any(Class.class));
    }

    private TravelPlan plan(String destinationName) {
        Destination destination = Destination.builder()
                .id(2L)
                .name(destinationName)
                .build();

        return TravelPlan.builder()
                .id(5L)
                .name("City break")
                .destination(destination)
                .build();
    }
}
