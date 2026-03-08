package com.tus.maintainx.service;

import com.tus.maintainx.dto.MaintenanceWindowCreateRequestDTO;
import com.tus.maintainx.dto.MaintenanceWindowResponseDTO;
import com.tus.maintainx.dto.MaintenanceWindowUpdateRequestDTO;
import com.tus.maintainx.entity.MaintenanceWindowEntity;
import com.tus.maintainx.entity.NetworkElementEntity;
import com.tus.maintainx.entity.UserEntity;
import com.tus.maintainx.exception.BadRequestException;
import com.tus.maintainx.exception.NotFoundException;
import com.tus.maintainx.repository.MaintenanceWindowRepository;
import com.tus.maintainx.repository.NetworkElementRepository;
import com.tus.maintainx.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MaintenanceWindowServiceTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void mockAuthenticatedUser(String username) {
        SecurityContext ctx = mock(SecurityContext.class);
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(username);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
    }

    @Test
    void createMntnceWindowTest() {
        MaintenanceWindowRepository mwRepo = mock(MaintenanceWindowRepository.class);
        NetworkElementRepository neRepo = mock(NetworkElementRepository.class);
        UserRepository userRepo = mock(UserRepository.class);

        MaintenanceWindowService service = new MaintenanceWindowService(mwRepo, neRepo, userRepo);

        mockAuthenticatedUser("engineer1");

        MaintenanceWindowCreateRequestDTO dto = new MaintenanceWindowCreateRequestDTO();
        dto.setTitle("Patch");
        dto.setDescription("Planned");
        dto.setStartTime(LocalDateTime.of(2026, 2, 18, 20, 0));
        dto.setEndTime(LocalDateTime.of(2026, 2, 18, 22, 0));
        dto.setNetworkElementIds(List.of(10L, 11L)); // used by service

        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setUsername("engineer1");
        when(userRepo.findByUsername("engineer1")).thenReturn(user);

        NetworkElementEntity e1 = new NetworkElementEntity();
        e1.setId(10L);
        e1.setName("NE1");

        NetworkElementEntity e2 = new NetworkElementEntity();
        e2.setId(11L);
        e2.setName("NE2");

        when(neRepo.findAllById(List.of(10L, 11L))).thenReturn(List.of(e1, e2));

        when(mwRepo.existsOverlappingMWindow(eq(10L), any(), any())).thenReturn(false);
        when(mwRepo.existsOverlappingMWindow(eq(11L), any(), any())).thenReturn(false);

        when(mwRepo.save(any(MaintenanceWindowEntity.class))).thenAnswer(inv -> {
            MaintenanceWindowEntity saved = inv.getArgument(0);
            saved.setId(99L);
            if (saved.getNetworkElements() == null) saved.setNetworkElements(new HashSet<>());
            return saved;
        });

        MaintenanceWindowResponseDTO resp = service.create(dto);

        assertEquals(99L, resp.getId());
        assertEquals("Patch", resp.getTitle());
        assertEquals("PENDING", resp.getWindowStatus());
        assertEquals("engineer1", resp.getRequestedByUsername());
        assertEquals(List.of(10L, 11L), resp.getNetworkElementIds());

        verify(userRepo).findByUsername("engineer1");
        verify(neRepo).findAllById(List.of(10L, 11L));
        verify(mwRepo).existsOverlappingMWindow(10L, dto.getStartTime(), dto.getEndTime());
        verify(mwRepo).existsOverlappingMWindow(11L, dto.getStartTime(), dto.getEndTime());
        verify(mwRepo).save(any(MaintenanceWindowEntity.class));
    }


    @Test
    void createMntncWindowInvalidNETest() {
        MaintenanceWindowRepository mwRepo = mock(MaintenanceWindowRepository.class);
        NetworkElementRepository neRepo = mock(NetworkElementRepository.class);
        UserRepository userRepo = mock(UserRepository.class);

        MaintenanceWindowService service = new MaintenanceWindowService(mwRepo, neRepo, userRepo);
        mockAuthenticatedUser("engineer1");

        MaintenanceWindowCreateRequestDTO dto = new MaintenanceWindowCreateRequestDTO();
        dto.setTitle("Patch");
        dto.setStartTime(LocalDateTime.of(2026, 2, 18, 20, 0));
        dto.setEndTime(LocalDateTime.of(2026, 2, 18, 22, 0));
        dto.setNetworkElementIds(List.of(10L, 11L)); // expects 2

        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setUsername("engineer1");
        when(userRepo.findByUsername("engineer1")).thenReturn(user);

        NetworkElementEntity e1 = new NetworkElementEntity();
        e1.setId(10L);
        e1.setName("NE1");
        when(neRepo.findAllById(List.of(10L, 11L))).thenReturn(List.of(e1));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.create(dto));
        assertTrue(ex.getMessage().toLowerCase().contains("invalid"));

        verify(mwRepo, never()).save(any());
    }

    @Test
    void createMWOverlapTest() {
        MaintenanceWindowRepository mwRepo = mock(MaintenanceWindowRepository.class);
        NetworkElementRepository neRepo = mock(NetworkElementRepository.class);
        UserRepository userRepo = mock(UserRepository.class);

        MaintenanceWindowService service = new MaintenanceWindowService(mwRepo, neRepo, userRepo);
        mockAuthenticatedUser("engineer1");

        MaintenanceWindowCreateRequestDTO dto = new MaintenanceWindowCreateRequestDTO();
        dto.setTitle("Patch");
        dto.setDescription("Planned");
        dto.setStartTime(LocalDateTime.of(2026, 2, 18, 20, 0));
        dto.setEndTime(LocalDateTime.of(2026, 2, 18, 22, 0));
        dto.setNetworkElementIds(List.of(10L, 11L));

        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setUsername("engineer1");
        when(userRepo.findByUsername("engineer1")).thenReturn(user);

        NetworkElementEntity e1 = new NetworkElementEntity();
        e1.setId(10L);
        e1.setName("NE1");

        NetworkElementEntity e2 = new NetworkElementEntity();
        e2.setId(11L);
        e2.setName("NE2");

        when(neRepo.findAllById(List.of(10L, 11L))).thenReturn(List.of(e1, e2));

        // no overlap for 10, overlap for 11
        when(mwRepo.existsOverlappingMWindow(eq(10L), any(), any())).thenReturn(false);
        when(mwRepo.existsOverlappingMWindow(eq(11L), any(), any())).thenReturn(true);

        Exception ex = assertThrows(Exception.class, () -> service.create(dto));
        assertTrue(ex.getMessage().contains("NE2"));

        verify(mwRepo, never()).save(any(MaintenanceWindowEntity.class));
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
    void getByIdWhenRequestedByNullShouldNotFail() {
        MaintenanceWindowRepository mwRepo = mock(MaintenanceWindowRepository.class);
        NetworkElementRepository neRepo = mock(NetworkElementRepository.class);
        UserRepository userRepo = mock(UserRepository.class);

        MaintenanceWindowService service = new MaintenanceWindowService(mwRepo, neRepo, userRepo);

        MaintenanceWindowEntity e = new MaintenanceWindowEntity();
        e.setId(10L);
        e.setTitle("Patch");
        e.setRequestedBy(null);
        e.setWindowStatus("PENDING");
        e.setNetworkElements(new HashSet<>());

        when(mwRepo.findById(10L)).thenReturn(Optional.of(e));

        MaintenanceWindowResponseDTO dto = service.getById(10L);

        assertEquals(10L, dto.getId());
        assertEquals("Patch", dto.getTitle());
        assertNull(dto.getRequestedByUsername());
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


    @Test
    void approverApprovedTest() {
        MaintenanceWindowRepository mwRepo = mock(MaintenanceWindowRepository.class);
        MaintenanceWindowService service = new MaintenanceWindowService(mwRepo, null, null);

        UserEntity requester = new UserEntity();
        requester.setUsername("engineer1");

        MaintenanceWindowEntity mw = new MaintenanceWindowEntity();
        mw.setId(10L);
        mw.setTitle("MW1");
        mw.setWindowStatus("PENDING");
        mw.setRequestedBy(requester);
        mw.setNetworkElements(new HashSet<>());
        mw.setRejectionReason("old reason"); // should be cleared
        when(mwRepo.findById(10L)).thenReturn(Optional.of(mw));

        SecurityContext ctx = mock(SecurityContext.class);
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("approver1");
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        when(mwRepo.save(any(MaintenanceWindowEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        MaintenanceWindowResponseDTO resp = service.approve(10L);

        assertEquals("APPROVED", resp.getWindowStatus());
        assertEquals("engineer1", resp.getRequestedByUsername());
        assertEquals(10L, resp.getId());

        assertEquals("APPROVED", mw.getWindowStatus());
        assertNull(mw.getRejectionReason());
        assertEquals("approver1", mw.getDecidedBy());

        verify(mwRepo).save(mw);
    }

    @Test
    void approverWhenNotPendingFailTest() {
        MaintenanceWindowRepository mwRepo = mock(MaintenanceWindowRepository.class);
        MaintenanceWindowService service = new MaintenanceWindowService(mwRepo, null, null);

        UserEntity requester = new UserEntity();
        requester.setUsername("engineer1");

        MaintenanceWindowEntity mw = new MaintenanceWindowEntity();
        mw.setId(10L);
        mw.setWindowStatus("APPROVED"); // already approved
        mw.setRequestedBy(requester);
        mw.setNetworkElements(new HashSet<>());
        when(mwRepo.findById(10L)).thenReturn(Optional.of(mw));


        BadRequestException ex = assertThrows(BadRequestException.class, () -> service.approve(10L));
        assertTrue(ex.getMessage().toLowerCase().contains("only pending"));

        verify(mwRepo, never()).save(any());
    }

    @Test
    void approverWindowNotFoundTest() {
        MaintenanceWindowRepository mwRepo = mock(MaintenanceWindowRepository.class);
        MaintenanceWindowService service = new MaintenanceWindowService(mwRepo, null, null);

        when(mwRepo.findById(10L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> service.approve(10L));
        assertTrue(ex.getMessage().toLowerCase().contains("not found"));
    }

    @Test
    void rejectWindowTest() {
        MaintenanceWindowRepository mwRepo = mock(MaintenanceWindowRepository.class);
        MaintenanceWindowService service = new MaintenanceWindowService(mwRepo, null, null);

        UserEntity requester = new UserEntity();
        requester.setUsername("engineer1");

        MaintenanceWindowEntity mw = new MaintenanceWindowEntity();
        mw.setId(10L);
        mw.setTitle("MW1");
        mw.setWindowStatus("PENDING");
        mw.setRequestedBy(requester);
        mw.setNetworkElements(new HashSet<>());
        when(mwRepo.findById(10L)).thenReturn(Optional.of(mw));

        SecurityContext ctx = mock(SecurityContext.class);
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("approver1");
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        when(mwRepo.save(any(MaintenanceWindowEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        MaintenanceWindowResponseDTO resp = service.reject(10L, "  Not safe now  ");

        assertEquals("REJECTED", resp.getWindowStatus());

        assertEquals("REJECTED", mw.getWindowStatus());
        assertEquals("Not safe now", mw.getRejectionReason());
        assertEquals("approver1", mw.getDecidedBy());

        verify(mwRepo).save(mw);
    }

    @Test
    void rejectReasonMissingTest() {
        MaintenanceWindowRepository mwRepo = mock(MaintenanceWindowRepository.class);
        MaintenanceWindowService service = new MaintenanceWindowService(mwRepo, null, null);

        UserEntity requester = new UserEntity();
        requester.setUsername("engineer1");

        MaintenanceWindowEntity mw = new MaintenanceWindowEntity();
        mw.setId(10L);
        mw.setWindowStatus("PENDING");
        mw.setRequestedBy(requester);
        mw.setNetworkElements(new HashSet<>());
        when(mwRepo.findById(10L)).thenReturn(Optional.of(mw));

        BadRequestException ex1 = assertThrows(BadRequestException.class, () -> service.reject(10L, null));
        assertTrue(ex1.getMessage().toLowerCase().contains("reason"));

        BadRequestException ex2 = assertThrows(BadRequestException.class, () -> service.reject(10L, "   "));
        assertTrue(ex2.getMessage().toLowerCase().contains("reason"));

        verify(mwRepo, never()).save(any());
    }

    @Test
    void rejectWhenNotPendingTest() {
        MaintenanceWindowRepository mwRepo = mock(MaintenanceWindowRepository.class);
        MaintenanceWindowService service = new MaintenanceWindowService(mwRepo, null, null);

        UserEntity requester = new UserEntity();
        requester.setUsername("engineer1");

        MaintenanceWindowEntity mw = new MaintenanceWindowEntity();
        mw.setId(10L);
        mw.setWindowStatus("APPROVED");
        mw.setRequestedBy(requester);
        mw.setNetworkElements(new HashSet<>());
        when(mwRepo.findById(10L)).thenReturn(Optional.of(mw));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> service.reject(10L, "No"));
        assertTrue(ex.getMessage().toLowerCase().contains("only pending"));

        verify(mwRepo, never()).save(any());
    }
}
