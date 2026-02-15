package com.tus.maintainx.entity;

import jakarta.persistence.*;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.HashSet;

public class MaintenanceWindowEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false, length = 25)
    private String windowStatus;

    @ManyToOne(optional = false)
    @JoinColumn(name = "requested_by", nullable = false)
    private UserEntity requestedBy;

    @ManyToMany
    @JoinTable(
            name = "maintenance_window_elements",
            joinColumns = @JoinColumn(name = "maintenance_window_id"),
            inverseJoinColumns = @JoinColumn(name = "network_element_id")
    )
    @Builder.Default
    private Set<NetworkElementEntity> networkElements = new HashSet<>();


}
