/**
 * DTO class for login.
 * Carries data between API layers.
 */

package com.tus.maintainx.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {

    private Long id;
    private String username;
    private String role;
    private String token;
    private String message;
}
