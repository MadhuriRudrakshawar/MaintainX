/**
 * Repository interface for audit log.
 * Handles database access for audit log.
 */

package com.tus.maintainx.repository;


import com.tus.maintainx.entity.AuditLogEntity;
import com.tus.maintainx.enums.AuditEntityType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {

    List<AuditLogEntity> findAllByOrderByCreatedAtDesc();

    List<AuditLogEntity> findByEntityTypeOrderByCreatedAtDesc(AuditEntityType entityType);

    List<AuditLogEntity> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(AuditEntityType entityType, Long entityId);
}

