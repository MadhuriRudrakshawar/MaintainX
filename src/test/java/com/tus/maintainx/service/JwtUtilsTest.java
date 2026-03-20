package com.tus.maintainx.service;

import com.tus.maintainx.config.JwtUtils;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilsTest {

    private static final String SECRET = "01234567890123456789012345678901";

    private JwtUtils createJwtUtils() {
        JwtUtils jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtils, "expirationSeconds", 3600L);
        return jwtUtils;
    }

    @Test
    void shouldGenerateParseAndValidateTokenWithRole() {
        JwtUtils jwtUtils = createJwtUtils();

        String token = jwtUtils.generateToken("alice", "APPROVER");

        assertEquals("alice", jwtUtils.getUsername(token));
        assertEquals("APPROVER", jwtUtils.getRole(token));
        assertTrue(jwtUtils.isValid(token));
    }

    @Test
    void shouldDefaultRoleToUserWhenClaimMissing() {
        JwtUtils jwtUtils = createJwtUtils();

        String token = Jwts.builder()
                .subject("bob")
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
                .compact();

        assertEquals("USER", jwtUtils.getRole(token));
    }

    @Test
    void shouldReturnFalseForInvalidToken() {
        JwtUtils jwtUtils = createJwtUtils();

        assertFalse(jwtUtils.isValid("not-a-token"));
    }
}
