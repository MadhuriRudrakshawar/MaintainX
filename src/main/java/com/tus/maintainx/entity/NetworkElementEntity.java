package com.tus.maintainx.entity;


import jakarta.persistence.*;
import lombok.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "network_element")
@Data
@NoArgsConstructor @AllArgsConstructor
public class NetworkElementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "element_code", nullable = false, unique = true, length = 40)
    private String elementCode;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "element_type", nullable = false, length = 30)
    private String elementType;

    @Column(nullable = false, length = 60)
    private String region;

    @Column(nullable = false, length = 20)
    private String status;


}
