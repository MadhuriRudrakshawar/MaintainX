package com.tus.maintainx.service;

import com.tus.maintainx.dto.NetworkElementCreateDTO;
import com.tus.maintainx.dto.NetworkElementResponseDTO;
import com.tus.maintainx.entity.NetworkElementEntity;
import com.tus.maintainx.repository.NetworkElementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NetworkElementServiceTest {

    @Mock
    private NetworkElementRepository repo;

    @InjectMocks
    private NetworkElementService service;

    private NetworkElementCreateDTO dto;

    @BeforeEach
    void setup() {
        dto = new NetworkElementCreateDTO(
                "  NE-001  ",
                "  Core Router  ",
                "  ROUTER  ",
                "  Dublin  ",
                "ACTIVE"
        );
    }

    @Test
    void createAndSaveEntityTest() {

        when(repo.save(any(NetworkElementEntity.class))).thenAnswer(invocation -> {
            NetworkElementEntity e = invocation.getArgument(0);
            e.setId(10L);
            return e;
        });

        NetworkElementResponseDTO created = service.create(dto);

        assertNotNull(created.getId());
        assertEquals("NE-001", created.getElementCode());
        assertEquals("Core Router", created.getName());
        assertEquals("ROUTER", created.getElementType());
        assertEquals("Dublin", created.getRegion());
        assertEquals("ACTIVE", created.getStatus());

        verify(repo, times(1)).save(any(NetworkElementEntity.class));
    }


    @Test
    void deactivateStatusCheck() {
        NetworkElementEntity existing = new NetworkElementEntity(
                1L, "NE-001", "Core Router", "ROUTER", "Dublin", "ACTIVE"
        );

        when(repo.findById(1L)).thenReturn(Optional.of(existing));
        when(repo.save(any(NetworkElementEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NetworkElementResponseDTO result = service.deactivate(1L);

        assertEquals("DEACTIVE", result.getStatus());
        verify(repo).save(existing);
    }

    @Test
    void activateStatusCheck() {
        NetworkElementEntity existing = new NetworkElementEntity(
                1L, "NE-001", "Core Router", "ROUTER", "Dublin", "DEACTIVE"
        );

        when(repo.findById(1L)).thenReturn(Optional.of(existing));
        when(repo.save(any(NetworkElementEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NetworkElementResponseDTO result = service.activate(1L);

        assertEquals("ACTIVE", result.getStatus());
        verify(repo).save(existing);
    }

    @Test
    void getAllTest() {
        NetworkElementEntity e1 = new NetworkElementEntity(
                1L, "NE-001", "Core Router", "ROUTER", "Dublin", "ACTIVE"
        );
        NetworkElementEntity e2 = new NetworkElementEntity(
                2L, "NE-002", "Edge Switch", "SWITCH", "Cork", "DEACTIVE"
        );

        when(repo.findAll()).thenReturn(java.util.List.of(e1, e2));

        var result = service.getAll();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("NE-001", result.get(0).getElementCode());
        assertEquals("Cork", result.get(1).getRegion());
        verify(repo).findAll();
    }

    @Test
    void getByElementId_NotFound() {
        when(repo.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.getByElementId(999L));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void updateHappyPathTest() {
        NetworkElementEntity existing = new NetworkElementEntity(
                1L, "NE-OLD", "Old", "OLDTYPE", "OldRegion", "DEACTIVE"
        );

        NetworkElementCreateDTO updateDto = new NetworkElementCreateDTO(
                "  NE-001  ", "  Core Router  ", "  ROUTER  ", "  Dublin  ", "ACTIVE"
        );

        when(repo.findById(1L)).thenReturn(Optional.of(existing));
        when(repo.save(any(NetworkElementEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        NetworkElementResponseDTO updated = service.update(1L, updateDto);

        assertEquals(1L, updated.getId());
        assertEquals("NE-001", updated.getElementCode());
        assertEquals("Core Router", updated.getName());
        assertEquals("ROUTER", updated.getElementType());
        assertEquals("Dublin", updated.getRegion());
        assertEquals("ACTIVE", updated.getStatus());
        verify(repo).findById(1L);
        verify(repo).save(existing);
    }

    @Test
    void update_NotFound() {
        when(repo.findById(123L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.update(123L, dto));

        assertEquals(404, ex.getStatusCode().value());
        verify(repo, never()).save(any());
    }

    @Test
    void deactivate_NotFound() {
        when(repo.findById(123L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.deactivate(123L));

        assertEquals(404, ex.getStatusCode().value());
        verify(repo, never()).save(any());
    }

    @Test
    void activate_NotFound() {
        when(repo.findById(123L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.activate(123L));

        assertEquals(404, ex.getStatusCode().value());
        verify(repo, never()).save(any());
    }

    @Test
    void deleteEntityNotExistedTest() {
        when(repo.existsById(123L)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.delete(123L));

        assertEquals(404, ex.getStatusCode().value());
        verify(repo, never()).deleteById(anyLong());
    }

    @Test
    void deleteExistedEntityTest() {
        when(repo.existsById(5L)).thenReturn(true);

        service.delete(5L);

        verify(repo).deleteById(5L);
    }


}
