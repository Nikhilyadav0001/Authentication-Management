package com.auth.authbackend.security;

import com.auth.authbackend.entity.Provider;
import com.auth.authbackend.entity.RefreshToken;
import com.auth.authbackend.entity.User;
import com.auth.authbackend.repository.RefreshTokenRepository;
import com.auth.authbackend.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final CookieService cookieService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.auth.frontend.success-redirect:http://localhost:5173}")
    private String frontEndSuccessUrl;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        logger.info("Successful authentication");
        logger.info(authentication.toString());

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // Identify provider:
        String registrationId = "unknown";
        if (authentication instanceof OAuth2AuthenticationToken token) {
            registrationId = token.getAuthorizedClientRegistrationId();
        }

        logger.info("registrationId: {}", registrationId);
        logger.info("user attributes: {}", oAuth2User.getAttributes());

        User user;
        switch (registrationId) {
            case "google" -> {
                String googleId = oAuth2User.getAttributes().getOrDefault("sub", "").toString();
                String email = oAuth2User.getAttributes().getOrDefault("email", "").toString();
                String name = oAuth2User.getAttributes().getOrDefault("name", "").toString();
                String picture = oAuth2User.getAttributes().getOrDefault("picture", "").toString();

                User newUser = User.builder()
                        .email(email)
                        .name(name)
                        .image(picture)
                        .enabled(true)
                        .provider(Provider.GOOGLE)
                        .build();

                user = userRepository.findByEmail(email).map(existingUser -> {
                    // Update profile info if missing
                    boolean updated = false;
                    if ((existingUser.getImage() == null || existingUser.getImage().isBlank()) && !picture.isBlank()) {
                        existingUser.setImage(picture);
                        updated = true;
                    }
                    if ((existingUser.getName() == null || existingUser.getName().isBlank()) && !name.isBlank()) {
                        existingUser.setName(name);
                        updated = true;
                    }
                    return updated ? userRepository.save(existingUser) : existingUser;
                }).orElseGet(() -> userRepository.save(newUser));
            }

            default -> throw new RuntimeException("Invalid registration id: " + registrationId);
        }

        // Create and save refresh token:
        String jti = UUID.randomUUID().toString();
        RefreshToken refreshTokenOb = RefreshToken.builder()
                .jti(jti)
                .user(user)
                .revoked(false)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
                .build();

        refreshTokenRepository.save(refreshTokenOb);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user, refreshTokenOb.getJti());
        cookieService.attachRefreshCookie(response, refreshToken, (int) jwtService.getRefreshTtlSeconds());

        // In a full SPA flow, redirect to frontend with token (or let frontend exchange cookie):
        // response.sendRedirect(frontEndSuccessUrl + "?token=" + accessToken);
        // For testing backend directly in browser/client:
        response.getWriter().write("Login successful");
    }
}
