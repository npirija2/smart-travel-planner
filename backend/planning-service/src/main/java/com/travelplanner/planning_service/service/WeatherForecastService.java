package com.travelplanner.planning_service.service;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.travelplanner.planning_service.dto.WeatherForecastResponseDTO;
import com.travelplanner.planning_service.exception.ResourceNotFoundException;
import com.travelplanner.planning_service.model.Location;
import com.travelplanner.planning_service.model.TravelPlan;
import com.travelplanner.planning_service.repository.LocationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherForecastService {

    private final LocationRepository locationRepository;

    @Qualifier("directTemplate")
    private final RestTemplate restTemplate;

    @Value("${weather.api.forecast-url:https://api.open-meteo.com/v1/forecast}")
    private String forecastUrl;

    @Value("${weather.api.geocoding-url:https://geocoding-api.open-meteo.com/v1/search}")
    private String geocodingUrl;

    public List<WeatherForecastResponseDTO> getForecastForPlan(TravelPlan plan, List<LocalDate> dates) {
        if (dates.isEmpty()) {
            return List.of();
        }

        Coordinates coordinates = resolveCoordinates(plan);
        ForecastResponse forecastResponse = fetchForecast(coordinates, dates.get(0), dates.get(dates.size() - 1));
        DailyForecast dailyForecast = Objects.requireNonNullElseGet(forecastResponse.daily(), DailyForecast::empty);

        Map<LocalDate, Integer> temperaturesByDate = dailyForecast.temperatureByDate();
        Map<LocalDate, Integer> weatherCodesByDate = dailyForecast.weatherCodeByDate();

        List<WeatherForecastResponseDTO> forecast = dates.stream()
                .filter(date -> temperaturesByDate.containsKey(date) && weatherCodesByDate.containsKey(date))
                .map(date -> buildForecast(date, temperaturesByDate.get(date), weatherCodesByDate.get(date)))
                .toList();

        if (forecast.isEmpty()) {
            throw new ResourceNotFoundException("Weather forecast is not available for the selected travel dates yet");
        }

        return forecast;
    }

    private Coordinates resolveCoordinates(TravelPlan plan) {
        List<Location> locations = locationRepository.findByDestinationId(plan.getDestination().getId()).stream()
                .filter(location -> location.getLatitude() != null && location.getLongitude() != null)
                .toList();

        if (!locations.isEmpty()) {
            double averageLatitude = locations.stream().mapToDouble(Location::getLatitude).average().orElseThrow();
            double averageLongitude = locations.stream().mapToDouble(Location::getLongitude).average().orElseThrow();
            return new Coordinates(averageLatitude, averageLongitude);
        }

        return geocodeDestination(plan.getDestination().getName());
    }

    private Coordinates geocodeDestination(String destinationName) {
        URI uri = UriComponentsBuilder.fromHttpUrl(geocodingUrl)
                .queryParam("name", destinationName)
                .queryParam("count", 1)
                .queryParam("language", "en")
                .queryParam("format", "json")
                .build(true)
                .toUri();

        try {
            ResponseEntity<GeocodingResponse> response = restTemplate.getForEntity(uri, GeocodingResponse.class);
            List<GeocodingResult> results = response.getBody() == null ? List.of() : response.getBody().results();

            if (results == null || results.isEmpty()) {
                throw new ResourceNotFoundException("Could not resolve coordinates for destination " + destinationName);
            }

            GeocodingResult result = results.get(0);
            return new Coordinates(result.latitude(), result.longitude());
        } catch (RestClientException exception) {
            log.error("Failed to geocode destination {}", destinationName, exception);
            throw new RuntimeException("Unable to load destination coordinates from the weather provider", exception);
        }
    }

    private ForecastResponse fetchForecast(Coordinates coordinates, LocalDate startDate, LocalDate endDate) {
        URI uri = UriComponentsBuilder.fromHttpUrl(forecastUrl)
                .queryParam("latitude", coordinates.latitude())
                .queryParam("longitude", coordinates.longitude())
                .queryParam("daily", "weather_code,temperature_2m_max")
                .queryParam("timezone", "auto")
                .queryParam("start_date", startDate)
                .queryParam("end_date", endDate)
                .build(true)
                .toUri();

        try {
            ResponseEntity<ForecastResponse> response = restTemplate.getForEntity(uri, ForecastResponse.class);
            if (response.getBody() == null) {
                throw new ResourceNotFoundException("Weather provider returned no forecast data");
            }
            return response.getBody();
        } catch (RestClientException exception) {
            log.error("Failed to load weather forecast for coordinates {}, {}", coordinates.latitude(), coordinates.longitude(), exception);
            throw new RuntimeException("Unable to load weather forecast from the external provider", exception);
        }
    }

    private WeatherForecastResponseDTO buildForecast(LocalDate date, Integer temperature, Integer weatherCode) {
        String condition = weatherCondition(weatherCode);
        return WeatherForecastResponseDTO.builder()
                .date(date)
                .condition(condition)
                .temperatureCelsius(temperature)
                .recommendation(recommendationFor(condition))
                .suggestedTimeslot(suggestedTimeslotFor(condition))
                .build();
    }

    private String weatherCondition(Integer weatherCode) {
        if (weatherCode == null) {
            return "Unknown";
        }

        return switch (weatherCode) {
            case 0 -> "Sunny";
            case 1, 2, 3 -> "Cloudy";
            case 45, 48 -> "Foggy";
            case 51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82 -> "Rain";
            case 71, 73, 75, 77, 85, 86 -> "Snow";
            case 95, 96, 99 -> "Stormy";
            default -> "Windy";
        };
    }

    private String recommendationFor(String condition) {
        return switch (condition) {
            case "Sunny" -> "Great day for outdoor attractions and walking routes.";
            case "Cloudy" -> "Flexible weather for mixed indoor and outdoor activities.";
            case "Rain", "Stormy" -> "Prioritize museums, dining, and covered attractions.";
            case "Snow", "Foggy" -> "Keep travel distances short and favor indoor options with flexible timing.";
            default -> "Keep transit time light and avoid overly packed routes.";
        };
    }

    private String suggestedTimeslotFor(String condition) {
        return switch (condition) {
            case "Sunny" -> "Morning";
            case "Cloudy" -> "Afternoon";
            case "Rain", "Stormy", "Foggy" -> "Evening";
            case "Snow" -> "Midday";
            default -> "Flexible";
        };
    }

    record Coordinates(double latitude, double longitude) {}

    record GeocodingResponse(List<GeocodingResult> results) {}

    record GeocodingResult(double latitude, double longitude) {}

    record ForecastResponse(DailyForecast daily) {}

    record DailyForecast(
            List<LocalDate> time,
            @JsonProperty("weather_code") List<Integer> weatherCode,
            @JsonProperty("temperature_2m_max") List<Double> temperatureMax
    ) {
        static DailyForecast empty() {
            return new DailyForecast(List.of(), List.of(), List.of());
        }

        Map<LocalDate, Integer> temperatureByDate() {
            return indexByDate(time, temperatureMax == null ? List.of() : temperatureMax)
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> (int) Math.round(entry.getValue())));
        }

        Map<LocalDate, Integer> weatherCodeByDate() {
            return indexByDate(time, weatherCode == null ? List.of() : weatherCode);
        }

        private static <T> Map<LocalDate, T> indexByDate(List<LocalDate> dates, List<T> values) {
            int size = Math.min(dates == null ? 0 : dates.size(), values.size());
            return java.util.stream.IntStream.range(0, size)
                    .boxed()
                    .collect(Collectors.toMap(dates::get, values::get));
        }
    }
}
