package com.tus.maintainx.controller;


import com.tus.maintainx.dto.LoginRequest;
import com.tus.maintainx.dto.LoginResponse;
import com.tus.maintainx.entity.UserEntity;
import com.tus.maintainx.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String S_USER = "LOGIN_USER";
    private static final String S_ROLE = "LOGIN_ROLE";
    private final UserRepository userRepo;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req, HttpSession session) {

        String username = (req.getUsername() == null) ? "" : req.getUsername().trim();
        String password = (req.getPassword() == null) ? "" : req.getPassword();

        UserEntity user = userRepo.findByUsername(username);

        if (user == null || !user.getPassword().equals(password)) {
            return ResponseEntity.status(401)
                    .body(new LoginResponse(username, null, "Login denied"));
        }

        session.setAttribute(S_USER, user.getUsername());
        session.setAttribute(S_ROLE, user.getRole());

        return ResponseEntity.ok(new LoginResponse(user.getUsername(), user.getRole(), "Login Successful!!!"));

    }

    @PostMapping("/logout")
    public ResponseEntity<LoginResponse> logout(HttpSession session) {

        String username = (String) session.getAttribute(S_USER);
        String role = (String) session.getAttribute(S_ROLE);

        session.invalidate();
        return ResponseEntity.ok(new LoginResponse(username, role, "Logged out"));
    }

}
