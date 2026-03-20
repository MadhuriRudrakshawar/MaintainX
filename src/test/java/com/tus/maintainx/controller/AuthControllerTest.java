package com.tus.maintainx.controller;


import com.tus.maintainx.config.JwtUtils;
import com.tus.maintainx.dto.LoginRequest;
import com.tus.maintainx.entity.UserEntity;
import com.tus.maintainx.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    UserRepository userRepo;
    @MockitoBean
    AuthenticationManager authManager;
    @MockitoBean
    JwtUtils jwtUtils;

    @Test
    void login_validCredentials() throws Exception {
        LoginRequest req = new LoginRequest("engineer1@mail.com", "pass");

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);

        UserEntity user = new UserEntity();
        user.setId(10L);
        user.setUsername("engineer1@mail.com");
        user.setRole("ENGINEER");

        when(userRepo.findByUsername("engineer1@mail.com")).thenReturn(user);
        when(jwtUtils.generateToken("engineer1@mail.com", "ENGINEER")).thenReturn("jwt-token");

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("engineer1@mail.com"))
                .andExpect(jsonPath("$.role").value("ENGINEER"))
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.message").value("Login successful"));
    }

    @Test
    void login_invalidCredentials() throws Exception {
        LoginRequest req = new LoginRequest("bad@mail.com", "bad");

        when(authManager.authenticate(any())).thenThrow(new RuntimeException("bad creds"));

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void login_notAuthenticated() throws Exception {
        LoginRequest req = new LoginRequest("engineer1@mail.com", "pass");

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.username").value("engineer1@mail.com"))
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void loginAuthenticatedUserMissing() throws Exception {
        LoginRequest req = new LoginRequest("engineer1@mail.com", "pass");

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(userRepo.findByUsername("engineer1@mail.com")).thenReturn(null);

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.username").value("engineer1@mail.com"))
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }


    @Test
    void login_invalidEmail() throws Exception {
        LoginRequest req = new LoginRequest("bad", "pass");

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username must be a valid email address"));
    }

    @Test
    void logout_returns200LoggedOut() throws Exception {
        mvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out"));
    }
}
