package com.myfinbank.controller;

import com.myfinbank.dto.*;
import com.myfinbank.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticazione", description = "Gestione login, registrazione e refresh token")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/register")
    @Operation(summary = "Registra un nuovo utente")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        logger.info("Inizio registrazione per utente: {}", request.getUsername());
        try {
            authService.register(request);
            logger.info("Registrazione completata con successo per utente: {}", request.getUsername());
            return ResponseEntity.status(201).build();
        } catch (IllegalArgumentException ex) {
            logger.error("Errore durante la registrazione: {}", ex.getMessage(), ex);
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        AuthResponse authResponse = authService.login(request);

        // refreshToken come cookie HttpOnly
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", authResponse.getRefreshToken())
                .httpOnly(true)
                .secure(true) // in prod: solo HTTPS
                .path("/api/auth/refresh") // cookie inviato solo a quell'endpoint
                .sameSite("Strict")
                .maxAge(Duration.ofDays(7))
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(Map.of(
                        "accessToken", authResponse.getAccessToken(),
                        "username", authResponse.getUsername()
                ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@CookieValue("refreshToken") String refreshToken) {
        AuthResponse newTokens = authService.refreshAccessToken(refreshToken);

        // opzionale: rigenerare anche refreshToken e risettare il cookie
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", newTokens.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/api/auth/refresh")
                .sameSite("Strict")
                .maxAge(Duration.ofDays(7))
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(Map.of("accessToken", newTokens.getAccessToken()));
    }


    @PostMapping("/logout")
    @Operation(summary = "Logout utente")
    public ResponseEntity<?> logout(@RequestBody TokenRefreshRequest request) {
        logger.info("Logout in corso per token: {}", request.getRefreshToken());
        authService.logout(request.getRefreshToken());
        logger.info("Logout completato con successo");
        return ResponseEntity.ok().build();
    }
}
