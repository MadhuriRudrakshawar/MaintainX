package com.tus.maintainx.controller;

import com.tus.maintainx.config.JwtUtils;
import com.tus.maintainx.dto.LoginRequest;
import com.tus.maintainx.dto.LoginResponse;
import com.tus.maintainx.entity.UserEntity;
import com.tus.maintainx.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "APIs for user login and logout")
public class AuthController {

    private static final String LOGIN_DENIED = "Invalid username or password";

    private final UserRepository userRepo;
    private final AuthenticationManager authManager;
    private final JwtUtils jwtUtils;

    @Operation(summary = "Login user", description = "Authenticates user and returns JWT token")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {

        String username = (req.getUsername() == null) ? "" : req.getUsername().trim();
        String password = (req.getPassword() == null) ? "" : req.getPassword();

        try {
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            if (!authentication.isAuthenticated()) {
                return ResponseEntity.status(401)
                        .body(new LoginResponse(null, username, null, null, LOGIN_DENIED));
            }

            UserEntity user = userRepo.findByUsername(username);
            if (user == null) {
                return ResponseEntity.status(401)
                        .body(new LoginResponse(null, username, null, null, LOGIN_DENIED));
            }

            String role = (user.getRole() == null) ? "USER" : user.getRole().trim().toUpperCase();
            String token = jwtUtils.generateToken(user.getUsername(), role);

            return ResponseEntity.ok(new LoginResponse(user.getId(), user.getUsername(), role, token, "Login successful"));
        } catch (Exception ex) {
            return ResponseEntity.status(401)
                    .body(new LoginResponse(null, username, null, null, LOGIN_DENIED));
        }
    }

    @Operation(summary = "Logout user", description = "Logs out the current user")
    @PostMapping("/logout")
    public ResponseEntity<LoginResponse> logout() {
        return ResponseEntity.ok(new LoginResponse(null, null, null, null, "Logged out"));
    }
}