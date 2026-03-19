/**
 * Entity class for network element.
 * Maps network element data to a database table.
 */

package com.tus.maintainx.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "network_element")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NetworkElementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String elementCode;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 30)
    private String elementType;

    @Column(nullable = false, length = 60)
    private String region;

    @Column(nullable = false, length = 20)
    private String status;
}
