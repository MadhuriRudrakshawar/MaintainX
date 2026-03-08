package com.tus.maintainx.repository;

import com.tus.maintainx.entity.MaintenanceWindowEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface MaintenanceWindowRepository extends JpaRepository<MaintenanceWindowEntity, Long> {

    boolean existsByTitle(String title);


    @Query("""
                select (count(mw) > 0)
                from MaintenanceWindowEntity mw
                join mw.networkElements ne
                where ne.id = :elementId
                  and mw.startTime < :requestedEnd
                  and mw.endTime > :requestedStart
                  and mw.windowStatus <> 'REJECTED'
            """)
    boolean existsOverlappingMWindow(
            @Param("elementId") Long elementId,
            @Param("requestedStart") LocalDateTime requestedStart,
            @Param("requestedEnd") LocalDateTime requestedEnd
    );


}
