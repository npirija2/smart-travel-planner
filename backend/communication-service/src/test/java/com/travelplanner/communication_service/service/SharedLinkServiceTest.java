package com.travelplanner.communication_service.service;

import com.travelplanner.communication_service.dto.SharedLinkRequestDTO;
import com.travelplanner.communication_service.dto.SharedLinkResponseDTO;
import com.travelplanner.communication_service.exception.ResourceNotFoundException;
import com.travelplanner.communication_service.model.SharedLink;
import com.travelplanner.communication_service.repository.SharedLinkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SharedLinkServiceTest {

    @Mock
    private SharedLinkRepository sharedLinkRepository;

    @InjectMocks
    private SharedLinkService sharedLinkService;

    private SharedLink sharedLink;
    private SharedLinkRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        // Priprema modela koji glumi podatak iz baze
        sharedLink = new SharedLink();
        sharedLink.setId(1);
        sharedLink.setUrl("http://example.com");
        sharedLink.setPlanId(10);
        sharedLink.setType("SOCIAL");

        // Priprema DTO-a koji dolazi s frontenda
        requestDTO = new SharedLinkRequestDTO();
        requestDTO.setUrl("http://example.com");
        requestDTO.setPlanId(10);
        requestDTO.setType("SOCIAL");
    }

    @Test
    void shouldCreateSharedLinkSuccessfully() {
        // Kada se pozove save, vrati naš pripremljeni link
        when(sharedLinkRepository.save(any(SharedLink.class))).thenReturn(sharedLink);

        SharedLinkResponseDTO response = sharedLinkService.createSharedLink(requestDTO);

        assertNotNull(response);
        assertEquals("http://example.com", response.getUrl());
        verify(sharedLinkRepository, times(1)).save(any(SharedLink.class));
    }

    @Test
    void shouldReturnAllSharedLinks() {
        when(sharedLinkRepository.findAll()).thenReturn(List.of(sharedLink));

        List<SharedLinkResponseDTO> result = sharedLinkService.getAllSharedLinks();

        assertEquals(1, result.size());
        assertEquals(sharedLink.getUrl(), result.get(0).getUrl());
    }

    @Test
    void shouldGetSharedLinkById() {
        when(sharedLinkRepository.findById(1)).thenReturn(Optional.of(sharedLink));

        SharedLinkResponseDTO response = sharedLinkService.getSharedLinkById(1);

        assertNotNull(response);
        assertEquals(1, response.getId());
    }

    @Test
    void shouldThrowExceptionWhenLinkNotFound() {
        when(sharedLinkRepository.findById(99)).thenReturn(Optional.empty());

        // Provjeravamo baca li točno našu ResourceNotFoundException
        assertThrows(ResourceNotFoundException.class, () -> {
            sharedLinkService.getSharedLinkById(99);
        });
    }

    @Test
    void shouldUpdateSharedLink() {
        when(sharedLinkRepository.findById(1)).thenReturn(Optional.of(sharedLink));
        when(sharedLinkRepository.save(any(SharedLink.class))).thenReturn(sharedLink);

        requestDTO.setUrl("http://new-url.com");
        SharedLinkResponseDTO updated = sharedLinkService.updateSharedLink(1, requestDTO);

        assertNotNull(updated);
        verify(sharedLinkRepository).save(any(SharedLink.class));
    }

    @Test
    void shouldDeleteSharedLinkSuccessfully() {
        when(sharedLinkRepository.findById(1)).thenReturn(Optional.of(sharedLink));
        doNothing().when(sharedLinkRepository).delete(sharedLink);

        sharedLinkService.deleteSharedLink(1);

        verify(sharedLinkRepository, times(1)).delete(sharedLink);
    }
}