package com.auth.authbackend.controller;


import com.auth.authbackend.dto.LoginRequest;
import com.auth.authbackend.dto.TokenResponse;
import com.auth.authbackend.dto.UserDto;
import com.auth.authbackend.entity.RefreshToken;
import com.auth.authbackend.entity.User;
import com.auth.authbackend.repository.RefreshTokenRepository;
import com.auth.authbackend.repository.UserRepository;
import com.auth.authbackend.security.JwtService;
import com.auth.authbackend.service.AuthService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService  jwtService;
    private final ModelMapper modelMapper;
    private final RefreshTokenRepository refreshTokenRepository;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @RequestBody LoginRequest loginRequest
    ){
        //authenticate
        Authentication authentication=authenticate(loginRequest);
        User user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid Username or Password"));
        if (!user.isEnabled()) {
            throw new DisabledException("User is disabled");
        }

        String jti = UUID.randomUUID().toString();
        var refreshTokenOb = RefreshToken.builder()
                .jti(jti)
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
                .revoked(false)
                .build();

        //refresh token save--information
        refreshTokenRepository.save(refreshTokenOb);

        //token--generate
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user, refreshTokenOb.getJti());

        TokenResponse tokenResponse = TokenResponse.of(accessToken,refreshToken,jwtService.getAccessTtlSeconds(),modelMapper.map(user, UserDto.class));
        return ResponseEntity.ok(tokenResponse);
    }

    private Authentication authenticate(LoginRequest loginRequest) {
        try {
            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid Username or Password !!");
        }
    }
    @PostMapping("/register")
    public ResponseEntity<UserDto> RegisterUser(@RequestBody UserDto userDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(userDto));
    }

}
