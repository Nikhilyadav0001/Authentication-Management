package com.auth.authbackend;

import com.auth.authbackend.entity.User;
import com.auth.authbackend.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AuthBackendApplicationTests {

    @Autowired
    private JwtService jwtService;

    @Test
    void contextLoads() {
        assertNotNull(jwtService);
    }

    @Test
    void testJwtGenerationAndValidation() {
        User user = new User();
        UUID userId = UUID.randomUUID();
        user.setId(userId);
        user.setEmail("test@example.com");

        String token = jwtService.generateAccessToken(user);
        assertNotNull(token);
        assertEquals(userId, jwtService.getUserId(token));
        assertEquals("test@example.com", jwtService.getEmail(token));
        assertTrue(jwtService.isAccessToken(token));
    }

}
