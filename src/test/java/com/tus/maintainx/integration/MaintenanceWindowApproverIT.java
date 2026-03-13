package com.tus.maintainx.integration;

import com.tus.maintainx.MaintainXApplication;
import com.tus.maintainx.config.JwtUtils;
import com.tus.maintainx.entity.MaintenanceWindowEntity;
import com.tus.maintainx.entity.NetworkElementEntity;
import com.tus.maintainx.entity.UserEntity;
import com.tus.maintainx.repository.MaintenanceWindowRepository;
import com.tus.maintainx.repository.NetworkElementRepository;
import com.tus.maintainx.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = MaintainXApplication.class)
@AutoConfigureMockMvc
class MaintenanceWindowApproverIT {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    MaintenanceWindowRepository maintenanceWindowRepository;

    @MockitoBean
    NetworkElementRepository networkElementRepository;

    @MockitoBean
    UserRepository userRepository;

    @MockitoBean
    JwtUtils jwtUtils;

    @Test
    void approve_ShouldReturnApproved_AndSetDecidedBy() throws Exception {
        long id = 10L;
        String token = "mock-approver-token";

        UserEntity requester = new UserEntity();
        requester.setUsername("engineer1");

        NetworkElementEntity ne1 = new NetworkElementEntity();
        ne1.setId(10L);
        ne1.setName("NE1");

        Set<NetworkElementEntity> nes = new HashSet<>();
        nes.add(ne1);

        MaintenanceWindowEntity mw = new MaintenanceWindowEntity();
        mw.setId(id);
        mw.setTitle("MW1");
        mw.setWindowStatus("PENDING");
        mw.setRequestedBy(requester);
        mw.setNetworkElements(nes);
        mw.setRejectionReason("old reason");

        when(maintenanceWindowRepository.findById(id)).thenReturn(Optional.of(mw));
        when(maintenanceWindowRepository.save(any(MaintenanceWindowEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(jwtUtils.isValid(token)).thenReturn(true);
        when(jwtUtils.getUsername(token)).thenReturn("approver1");
        when(jwtUtils.getRole(token)).thenReturn("APPROVER");
        UserEntity approver = new UserEntity();
        approver.setUsername("approver1");
        approver.setRole("APPROVER");
        approver.setPassword("test-password");
        when(userRepository.findByUsername("approver1")).thenReturn(approver);

        mockMvc.perform(patch("/api/v1/maintenance-windows/{id}/approve", id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.windowStatus").value("APPROVED"))
                .andExpect(jsonPath("$.requestedByUsername").value("engineer1"))
                .andExpect(jsonPath("$.decidedBy").value("approver1"))
                .andExpect(jsonPath("$.rejectionReason").value(nullValue()));
    }

    @Test
    void reject_ShouldReturnRejected_AndSetReason_AndDecidedBy() throws Exception {
        long id = 11L;
        String token = "mock-approver-token";

        UserEntity requester = new UserEntity();
        requester.setUsername("engineer1");

        MaintenanceWindowEntity mw = new MaintenanceWindowEntity();
        mw.setId(id);
        mw.setTitle("MW2");
        mw.setWindowStatus("PENDING");
        mw.setRequestedBy(requester);
        mw.setNetworkElements(new HashSet<>());

        when(maintenanceWindowRepository.findById(id)).thenReturn(Optional.of(mw));
        when(maintenanceWindowRepository.save(any(MaintenanceWindowEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(jwtUtils.isValid(token)).thenReturn(true);
        when(jwtUtils.getUsername(token)).thenReturn("approver1");
        when(jwtUtils.getRole(token)).thenReturn("APPROVER");
        UserEntity approver = new UserEntity();
        approver.setUsername("approver1");
        approver.setRole("APPROVER");
        approver.setPassword("test-password");
        when(userRepository.findByUsername("approver1")).thenReturn(approver);

        String body = """
                { "reason": "Not safe now" }
                """;

        mockMvc.perform(patch("/api/v1/maintenance-windows/{id}/reject", id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$.windowStatus").value("REJECTED"))
                .andExpect(jsonPath("$.requestedByUsername").value("engineer1"))
                .andExpect(jsonPath("$.decidedBy").value("approver1"))
                .andExpect(jsonPath("$.rejectionReason").value("Not safe now"));
    }

    @Test
    void approve_WithoutAuth_ShouldBeRejectedBySecurity() throws Exception {
        mockMvc.perform(patch("/api/v1/maintenance-windows/{id}/approve", 10L))
                .andExpect(status().is4xxClientError());
    }
}
