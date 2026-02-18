package com.tus.maintainx.service;

import com.tus.maintainx.dto.MaintenanceWindowCreateRequestDTO;
import com.tus.maintainx.dto.MaintenanceWindowResponseDTO;
import com.tus.maintainx.dto.MaintenanceWindowUpdateRequestDTO;
import com.tus.maintainx.entity.MaintenanceWindowEntity;
import com.tus.maintainx.entity.NetworkElementEntity;
import com.tus.maintainx.entity.UserEntity;
import com.tus.maintainx.repository.MaintenanceWindowRepository;
import com.tus.maintainx.repository.NetworkElementRepository;
import com.tus.maintainx.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MaintenanceWindowServiceTest {

    @Test
    void createMntnceWindowTest() {
        MaintenanceWindowRepository mwRepo = mock(MaintenanceWindowRepository.class);
        NetworkElementRepository neRepo = mock(NetworkElementRepository.class);
        UserRepository userRepo = mock(UserRepository.class);

        MaintenanceWindowService service = new MaintenanceWindowService(mwRepo, neRepo, userRepo);

        MaintenanceWindowCreateRequestDTO dto = new MaintenanceWindowCreateRequestDTO();
        dto.setTitle("Patch");
        dto.setDescription("Planned");
        dto.setStartTime(LocalDateTime.of(2026, 2, 18, 20, 0));
        dto.setEndTime(LocalDateTime.of(2026, 2, 18, 22, 0));
        dto.setRequestedById(1L);
        dto.setNetworkElementIds(List.of(10L, 11L));

        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setUsername("engineer1");
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        NetworkElementEntity e1 = new NetworkElementEntity();
        e1.setId(10L);
        e1.setName("NE1");
        NetworkElementEntity e2 = new NetworkElementEntity();
        e2.setId(11L);
        e2.setName("NE2");
        when(neRepo.findAllById(List.of(10L, 11L))).thenReturn(List.of(e1, e2));

        when(mwRepo.save(any(MaintenanceWindowEntity.class))).thenAnswer(inv -> {
            MaintenanceWindowEntity saved = inv.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        MaintenanceWindowResponseDTO resp = service.create(dto);

        assertEquals(99L, resp.getId());
        assertEquals("Patch", resp.getTitle());
        assertEquals("PENDING", resp.getWindowStatus());
        assertEquals("engineer1", resp.getRequestedByUsername());

        verify(mwRepo).save(any(MaintenanceWindowEntity.class));
    }

    @Test
    void createMntncWindowInvalidNETest() {
        MaintenanceWindowRepository mwRepo = mock(MaintenanceWindowRepository.class);
        NetworkElementRepository neRepo = mock(NetworkElementRepository.class);
        UserRepository userRepo = mock(UserRepository.class);

        MaintenanceWindowService service = new MaintenanceWindowService(mwRepo, neRepo, userRepo);

        MaintenanceWindowCreateRequestDTO dto = new MaintenanceWindowCreateRequestDTO();
        dto.setTitle("Patch");
        dto.setStartTime(LocalDateTime.of(2026, 2, 18, 20, 0));
        dto.setEndTime(LocalDateTime.of(2026, 2, 18, 22, 0));
        dto.setRequestedById(1L);
        dto.setNetworkElementIds(List.of(10L, 11L)); // expects 2

        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setUsername("engineer1");
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        NetworkElementEntity e1 = new NetworkElementEntity();
        e1.setId(10L);
        e1.setName("NE1");
        when(neRepo.findAllById(List.of(10L, 11L))).thenReturn(List.of(e1));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.create(dto));
        assertTrue(ex.getMessage().toLowerCase().contains("invalid"));

        verify(mwRepo, never()).save(any());
    }

    @Test
    void getAllMWTest() {
        MaintenanceWindowRepository mwRepo = mock(MaintenanceWindowRepository.class);
        NetworkElementRepository neRepo = mock(NetworkElementRepository.class);
        UserRepository userRepo = mock(UserRepository.class);

        MaintenanceWindowService service = new MaintenanceWindowService(mwRepo, neRepo, userRepo);

        // entity 1
        UserEntity u = new UserEntity();
        u.setUsername("engineer1");

        MaintenanceWindowEntity e1 = new MaintenanceWindowEntity();
        e1.setId(1L);
        e1.setTitle("MW1");
        e1.setRequestedBy(u);
        e1.setWindowStatus("PENDING");
        e1.setNetworkElements(new HashSet<>());

        // entity 2
        MaintenanceWindowEntity e2 = new MaintenanceWindowEntity();
        e2.setId(2L);
        e2.setTitle("MW2");
        e2.setRequestedBy(u);
        e2.setWindowStatus("PENDING");
        e2.setNetworkElements(new HashSet<>());

        when(mwRepo.findAll()).thenReturn(List.of(e1, e2));

        List<MaintenanceWindowResponseDTO> result = service.getAll();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
    }

    @Test
    void getByIdMWTest() {
        MaintenanceWindowRepository mwRepo = mock(MaintenanceWindowRepository.class);
        NetworkElementRepository neRepo = mock(NetworkElementRepository.class);
        UserRepository userRepo = mock(UserRepository.class);

        MaintenanceWindowService service = new MaintenanceWindowService(mwRepo, neRepo, userRepo);

        UserEntity u = new UserEntity();
        u.setUsername("engineer1");

        MaintenanceWindowEntity e = new MaintenanceWindowEntity();
        e.setId(10L);
        e.setTitle("Patch");
        e.setRequestedBy(u);
        e.setWindowStatus("PENDING");
        e.setNetworkElements(new HashSet<>());

        when(mwRepo.findById(10L)).thenReturn(Optional.of(e));

        MaintenanceWindowResponseDTO dto = service.getById(10L);

        assertEquals(10L, dto.getId());
        assertEquals("Patch", dto.getTitle());
    }

    @Test
    void updateMWTest() {
        MaintenanceWindowRepository mwRepo = mock(MaintenanceWindowRepository.class);
        NetworkElementRepository neRepo = mock(NetworkElementRepository.class);
        UserRepository userRepo = mock(UserRepository.class);

        MaintenanceWindowService service = new MaintenanceWindowService(mwRepo, neRepo, userRepo);

        UserEntity u = new UserEntity();
        u.setUsername("engineer1");

        MaintenanceWindowEntity existing = new MaintenanceWindowEntity();
        existing.setId(10L);
        existing.setTitle("Old");
        existing.setDescription("Old desc");
        existing.setRequestedBy(u);
        existing.setWindowStatus("PENDING");
        existing.setNetworkElements(new HashSet<>());

        when(mwRepo.findById(10L)).thenReturn(Optional.of(existing));

        NetworkElementEntity n1 = new NetworkElementEntity();
        n1.setId(1L);
        n1.setName("NE1");

        NetworkElementEntity n2 = new NetworkElementEntity();
        n2.setId(2L);
        n2.setName("NE2");

        when(neRepo.findAllById(List.of(1L, 2L))).thenReturn(List.of(n1, n2));

        when(mwRepo.save(any(MaintenanceWindowEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        MaintenanceWindowUpdateRequestDTO dto = new MaintenanceWindowUpdateRequestDTO();
        dto.setTitle("Updated");
        dto.setDescription("New desc");
        dto.setStartTime(LocalDateTime.of(2026, 2, 18, 20, 0));
        dto.setEndTime(LocalDateTime.of(2026, 2, 18, 22, 0));
        dto.setNetworkElementIds(List.of(1L, 2L));

        MaintenanceWindowResponseDTO resp = service.update(10L, dto);

        assertEquals("Updated", resp.getTitle());
        assertEquals(2, resp.getNetworkElementIds().size());

        verify(mwRepo).save(any(MaintenanceWindowEntity.class));
    }

    @Test
    void deleteMWExistTest() {
        MaintenanceWindowRepository repo = mock(MaintenanceWindowRepository.class);
        MaintenanceWindowService service = new MaintenanceWindowService(repo, null, null);

        long id = 10L;
        when(repo.existsById(id)).thenReturn(true);

        service.delete(id);

        verify(repo).deleteById(id);
    }

    @Test
    void deleteMWNotExistTest() {
        MaintenanceWindowRepository repo = mock(MaintenanceWindowRepository.class);
        MaintenanceWindowService service = new MaintenanceWindowService(repo, null, null);

        long id = 99L;
        when(repo.existsById(id)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.delete(id));
        assertTrue(ex.getMessage().contains("not found"));

        verify(repo, never()).deleteById(anyLong());
    }
}
