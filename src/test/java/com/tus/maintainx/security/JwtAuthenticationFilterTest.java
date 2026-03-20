package com.tus.maintainx.security;

import com.tus.maintainx.config.JwtUtils;
import com.tus.maintainx.entity.UserEntity;
import com.tus.maintainx.repository.UserRepository;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldSkipWhenAuthorizationHeaderMissing() throws Exception {
        JwtUtils jwtUtils = mock(JwtUtils.class);
        UserRepository userRepository = mock(UserRepository.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtils, userRepository);
        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilterInternal(new MockHttpServletRequest(), new MockHttpServletResponse(), filterChain);

        verify(filterChain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(userRepository, never()).findByUsername(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void shouldSkipWhenAuthorizationHeaderIsNotBearer() throws Exception {
        JwtUtils jwtUtils = mock(JwtUtils.class);
        UserRepository userRepository = mock(UserRepository.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtils, userRepository);
        FilterChain filterChain = mock(FilterChain.class);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic abc");

        filter.doFilterInternal(request, new MockHttpServletResponse(), filterChain);

        verify(filterChain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(userRepository, never()).findByUsername(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void shouldSkipWhenTokenInvalid() throws Exception {
        JwtUtils jwtUtils = mock(JwtUtils.class);
        UserRepository userRepository = mock(UserRepository.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtils, userRepository);
        FilterChain filterChain = mock(FilterChain.class);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid-token");
        when(jwtUtils.isValid("invalid-token")).thenReturn(false);

        filter.doFilterInternal(request, new MockHttpServletResponse(), filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(userRepository, never()).findByUsername(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void shouldSkipWhenAuthenticationAlreadyPresent() throws Exception {
        JwtUtils jwtUtils = mock(JwtUtils.class);
        UserRepository userRepository = mock(UserRepository.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtils, userRepository);
        FilterChain filterChain = mock(FilterChain.class);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer existing-token");
        when(jwtUtils.isValid("existing-token")).thenReturn(true);

        Authentication existingAuth = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        filter.doFilterInternal(request, new MockHttpServletResponse(), filterChain);

        assertEquals(existingAuth, SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(userRepository, never()).findByUsername(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void shouldSkipWhenUserNotFound() throws Exception {
        JwtUtils jwtUtils = mock(JwtUtils.class);
        UserRepository userRepository = mock(UserRepository.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtils, userRepository);
        FilterChain filterChain = mock(FilterChain.class);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        when(jwtUtils.isValid("valid-token")).thenReturn(true);
        when(jwtUtils.getUsername("valid-token")).thenReturn("ghost");
        when(userRepository.findByUsername("ghost")).thenReturn(null);

        filter.doFilterInternal(request, new MockHttpServletResponse(), filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldAuthenticateAndNormalizeRole() throws Exception {
        JwtUtils jwtUtils = mock(JwtUtils.class);
        UserRepository userRepository = mock(UserRepository.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtils, userRepository);
        FilterChain filterChain = mock(FilterChain.class);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        request.setRemoteAddr("127.0.0.1");
        when(jwtUtils.isValid("valid-token")).thenReturn(true);
        when(jwtUtils.getUsername("valid-token")).thenReturn("alice");

        UserEntity user = new UserEntity();
        user.setUsername("alice");
        user.setRole(" approver ");
        when(userRepository.findByUsername("alice")).thenReturn(user);

        filter.doFilterInternal(request, new MockHttpServletResponse(), filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals("alice", authentication.getName());
        assertEquals("ROLE_APPROVER", authentication.getAuthorities().iterator().next().getAuthority());
        assertNotNull(authentication.getDetails());
        verify(filterChain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldDefaultRoleToUserWhenUserRoleNull() throws Exception {
        JwtUtils jwtUtils = mock(JwtUtils.class);
        UserRepository userRepository = mock(UserRepository.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtils, userRepository);
        FilterChain filterChain = mock(FilterChain.class);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        when(jwtUtils.isValid("valid-token")).thenReturn(true);
        when(jwtUtils.getUsername("valid-token")).thenReturn("bob");

        UserEntity user = new UserEntity();
        user.setUsername("bob");
        user.setRole(null);
        when(userRepository.findByUsername("bob")).thenReturn(user);

        filter.doFilterInternal(request, new MockHttpServletResponse(), filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals("ROLE_USER", authentication.getAuthorities().iterator().next().getAuthority());
        verify(filterChain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }
}
