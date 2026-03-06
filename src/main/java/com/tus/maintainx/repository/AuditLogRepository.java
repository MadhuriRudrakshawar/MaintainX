package com.tus.maintainx.repository;


import com.tus.maintainx.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @EntityGraph(attributePaths = "maintenanceWindow")
    List<AuditLog> findByMaintenanceWindow_IdOrderByCreatedAtAsc(Long maintenanceWindowId);

    @EntityGraph(attributePaths = "maintenanceWindow")
    List<AuditLog> findAllByOrderByCreatedAtAsc();
}
