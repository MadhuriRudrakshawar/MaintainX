package com.tus.maintainx.integration;

import com.tus.maintainx.config.JwtUtils;
import com.tus.maintainx.entity.AuditLogEntity;
import com.tus.maintainx.entity.MaintenanceWindowEntity;
import com.tus.maintainx.entity.NetworkElementEntity;
import com.tus.maintainx.entity.UserEntity;
import com.tus.maintainx.enums.AuditAction;
import com.tus.maintainx.enums.AuditEntityType;
import com.tus.maintainx.enums.ExecutionStatus;
import com.tus.maintainx.repository.AuditLogRepository;
import com.tus.maintainx.repository.MaintenanceWindowRepository;
import com.tus.maintainx.repository.NetworkElementRepository;
import com.tus.maintainx.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Component
public class KarateSpringContext {

    private static KarateSpringContext instance;

    private final AuditLogRepository auditLogRepository;
    private final MaintenanceWindowRepository maintenanceWindowRepository;
    private final NetworkElementRepository networkElementRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public KarateSpringContext(
            AuditLogRepository auditLogRepository,
            MaintenanceWindowRepository maintenanceWindowRepository,
            NetworkElementRepository networkElementRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtils jwtUtils
    ) {
        this.auditLogRepository = auditLogRepository;
        this.maintenanceWindowRepository = maintenanceWindowRepository;
        this.networkElementRepository = networkElementRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        instance = this;
    }

    public static KarateSpringContext getInstance() {
        if (instance == null) {
            throw new IllegalStateException("KarateSpringContext has not been initialized");
        }
        return instance;
    }

    public void reset() {
        auditLogRepository.deleteAll();
        maintenanceWindowRepository.deleteAll();
        networkElementRepository.deleteAll();
        userRepository.deleteAll();
    }

    public Map<String, Object> seedBaselineData() {
        reset();

        saveUser("admin", "ADMIN", "admin-pass");
        saveUser("approver1", "APPROVER", "approve-pass");
        UserEntity engineer = saveUser("engineer1", "ENGINEER", "engineer-pass");

        NetworkElementEntity coreRouter = networkElementRepository.save(
                new NetworkElementEntity(null, "NE-CORE-1", "Core Router", "ROUTER", "Dublin", "ACTIVE")
        );
        NetworkElementEntity accessSwitch = networkElementRepository.save(
                new NetworkElementEntity(null, "NE-SW-1", "Access Switch", "SWITCH", "Cork", "MAINTENANCE")
        );
        NetworkElementEntity edgeFirewall = networkElementRepository.save(
                new NetworkElementEntity(null, "NE-FW-1", "Edge Firewall", "FIREWALL", "Dublin", "ACTIVE")
        );

        MaintenanceWindowEntity approvedWindow = maintenanceWindowRepository.save(
                MaintenanceWindowEntity.builder()
                        .title("Core Router Upgrade")
                        .description("Firmware update")
                        .startTime(LocalDateTime.of(2026, 3, 10, 10, 15))
                        .endTime(LocalDateTime.of(2026, 3, 10, 12, 15))
                        .windowStatus("APPROVED")
                        .rejectionReason(null)
                        .decidedBy("approver1")
                        .executionStatus(ExecutionStatus.PLANNED)
                        .requestedBy(engineer)
                        .networkElements(linkedSet(coreRouter, accessSwitch))
                        .build()
        );

        MaintenanceWindowEntity pendingWindow = maintenanceWindowRepository.save(
                MaintenanceWindowEntity.builder()
                        .title("Firewall Inspection")
                        .description("Quarterly inspection")
                        .startTime(LocalDateTime.of(2026, 3, 11, 8, 0))
                        .endTime(LocalDateTime.of(2026, 3, 11, 9, 0))
                        .windowStatus("PENDING")
                        .rejectionReason(null)
                        .decidedBy("")
                        .executionStatus(ExecutionStatus.PLANNED)
                        .requestedBy(engineer)
                        .networkElements(linkedSet(edgeFirewall))
                        .build()
        );

        MaintenanceWindowEntity rejectedWindow = maintenanceWindowRepository.save(
                MaintenanceWindowEntity.builder()
                        .title("Switch Refresh")
                        .description("Deferred due to overlap")
                        .startTime(LocalDateTime.of(2026, 3, 10, 10, 45))
                        .endTime(LocalDateTime.of(2026, 3, 10, 11, 15))
                        .windowStatus("REJECTED")
                        .rejectionReason("Overlap detected")
                        .decidedBy("approver1")
                        .executionStatus(ExecutionStatus.PLANNED)
                        .requestedBy(engineer)
                        .networkElements(linkedSet(accessSwitch))
                        .build()
        );

        auditLogRepository.save(AuditLogEntity.builder()
                .entityType(AuditEntityType.MAINTENANCE_WINDOW)
                .entityId(approvedWindow.getId())
                .action(AuditAction.APPROVED)
                .username("approver1")
                .roleName("APPROVER")
                .details("Approved core router upgrade")
                .createdAt(LocalDateTime.of(2026, 3, 10, 9, 0))
                .build());
        auditLogRepository.save(AuditLogEntity.builder()
                .entityType(AuditEntityType.MAINTENANCE_WINDOW)
                .entityId(approvedWindow.getId())
                .action(AuditAction.CREATED)
                .username("engineer1")
                .roleName("ENGINEER")
                .details("Created core router upgrade")
                .createdAt(LocalDateTime.of(2026, 3, 10, 8, 0))
                .build());
        auditLogRepository.save(AuditLogEntity.builder()
                .entityType(AuditEntityType.NETWORK_ELEMENT)
                .entityId(coreRouter.getId())
                .action(AuditAction.UPDATED)
                .username("admin")
                .roleName("ADMIN")
                .details("Updated router metadata")
                .createdAt(LocalDateTime.of(2026, 3, 9, 17, 30))
                .build());

        return Map.of(
                "adminUsername", "admin",
                "adminPassword", "admin-pass",
                "approverUsername", "approver1",
                "approverPassword", "approve-pass",
                "engineerUsername", "engineer1",
                "engineerPassword", "engineer-pass",
                "approvedWindowId", approvedWindow.getId(),
                "pendingWindowId", pendingWindow.getId(),
                "rejectedWindowId", rejectedWindow.getId(),
                "coreRouterId", coreRouter.getId()
        );
    }

    public String tokenFor(String username) {
        UserEntity user = userRepository.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("Unknown user: " + username);
        }
        return jwtUtils.generateToken(user.getUsername(), user.getRole());
    }


    private UserEntity saveUser(String username, String role, String rawPassword) {
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(rawPassword));
        return userRepository.save(user);
    }

    private Set<NetworkElementEntity> linkedSet(NetworkElementEntity... elements) {
        Set<NetworkElementEntity> set = new LinkedHashSet<>();
        Collections.addAll(set, elements);
        return set;
    }
}
