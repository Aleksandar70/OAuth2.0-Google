package org.smg.google.home.controller;

import org.smg.google.home.request.AuthRequest;
import org.smg.google.home.request.RefreshTokenRequest;
import org.smg.google.home.token.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final String ACCESS_TOKEN = "accessToken";
    private static final String REFRESH_TOKEN = "refreshToken";

    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    //CUSTOM Implementation
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        if (isValidUser(authRequest)) {
            String accessToken = jwtTokenProvider.generateAccessToken(authRequest.getUsername());
            String refreshToken = jwtTokenProvider.generateRefreshToken(authRequest.getUsername());

            return ResponseEntity.ok(Map.of(
                    ACCESS_TOKEN, accessToken,
                    REFRESH_TOKEN, refreshToken
            ));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    //CUSTOM Implementation
    @PostMapping("/refresh")
    public ResponseEntity<?> getAccessTokenByRefreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.getRefreshToken();

        if (jwtTokenProvider.validateToken(refreshToken)) {
            String username = jwtTokenProvider.getUsername(refreshToken);
            String newAccessToken = jwtTokenProvider.generateAccessToken(username);

            return ResponseEntity.ok(Map.of(ACCESS_TOKEN, newAccessToken));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
        }

    }

    private boolean isValidUser(AuthRequest authRequest) {
        return "user".equals(authRequest.getUsername()) && "password".equals(authRequest.getPassword());
    }
}
