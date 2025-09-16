package com.myfinbank.controller;

import com.myfinbank.dto.*;
import com.myfinbank.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @Operation(summary = "Login utente e generazione token JWT")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        logger.info("Tentativo di login per utente: {}", request.getUsername());
        try {
            AuthResponse resp = authService.login(request);
            logger.info("Login riuscito per utente: {}", request.getUsername());
            return ResponseEntity.ok(resp);
        } catch (Exception ex) {
            logger.warn("Login fallito per utente: {}, motivo: {}", request.getUsername(), ex.getMessage(), ex);
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Genera un nuovo access token tramite refresh token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        logger.info("Richiesta di refresh token per token: {}", request.getRefreshToken());
        try {
            TokenRefreshResponse resp = authService.refreshToken(request);
            logger.info("Refresh token completato con successo");
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException ex) {
            logger.error("Errore durante il refresh token: {}", ex.getMessage(), ex);
            return ResponseEntity.status(403).body(ex.getMessage());
        } catch (RuntimeException ex) {
            logger.error("Eccezione durante il refresh token: {}", ex.getMessage(), ex);
            return ResponseEntity.status(403).body(ex.getMessage());
        }
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
