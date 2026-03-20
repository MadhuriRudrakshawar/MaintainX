/**
 * Entity class for user.
 * Maps user data to a database table.
 */

package com.tus.maintainx.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,  length = 50)
    private String username;

    @Column(nullable = false, length = 20)
    private String role;

    @Column(nullable = false, length = 255)
    private String password;


}