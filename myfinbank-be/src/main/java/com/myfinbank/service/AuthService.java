package com.myfinbank.service;

import com.myfinbank.dto.*;
import com.myfinbank.entity.RefreshToken;
import com.myfinbank.entity.User;
import com.myfinbank.exception.InvalidTokenException;
import com.myfinbank.repository.UserRepository;
import com.myfinbank.security.JwtTokenUtil;
import com.myfinbank.utils.Ruoli;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenUtil jwtTokenUtil;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       RefreshTokenService refreshTokenService,
                       JwtTokenUtil jwtTokenUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;

        this.refreshTokenService = refreshTokenService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Transactional
    public void register(RegisterRequest request) {
        logger.info("Avvio della registrazione per l'utente: {}", request.getUsername());

        if (userRepository.existsByEmail(request.getEmail())) {
            logger.warn("Registrazione fallita: email già in uso - {}", request.getEmail());
            throw new IllegalArgumentException("Email già in uso: " + request.getEmail());
        }

        String ruolo = request.getIsAdmin() ? String.valueOf(Ruoli.ROLE_ADMIN) : String.valueOf(Ruoli.ROLE_USER);
        if (ruolo == null) {
            logger.error("Ruolo non trovato durante la registrazione per l'utente: {}", request.getUsername());
            throw new IllegalStateException("Ruolo non trovato");
        }

        User u = new User();
        u.setUsername(request.getUsername());
        u.setEmail(request.getEmail());
        u.setPassword(passwordEncoder.encode(request.getPassword()));
        u.setNome(request.getNome());
        u.setCognome(request.getCognome());
        u.setCodiceFiscale(request.getCodiceFiscale());
        u.setDataNascita(request.getDataNascita());
        u.setRuolo(ruolo);

        userRepository.save(u);
        logger.info("Registrazione completata con successo per l'utente: {}", request.getUsername());
    }

    public AuthResponse login(AuthRequest request) {
        logger.info("Tentativo di login per l'utente: {}", request.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    logger.error("Utente non trovato durante il login: {}", request.getUsername());
                    return new RuntimeException("Utente non trovato");
                });

        logger.info("Login riuscito per l'utente: {}", request.getUsername());

        String accessToken = jwtTokenUtil.generateAccessToken(user.getUsername());
        String refreshToken = jwtTokenUtil.refreshToken(user);

        refreshTokenService.createRefreshToken(user.getUsername(), refreshToken);
        logger.info("Token di refresh generato ----> " + refreshToken);
        logger.info("Token di accesso e refresh generati per l'utente: {}", user.getUsername());

        return new AuthResponse(accessToken, refreshToken, user.getUsername());
    }

    public AuthResponse refreshAccessToken(String refreshToken) {
        logger.info("Richiesta di refresh del token di accesso con il token di refresh: {}", refreshToken);

        RefreshToken stored = refreshTokenService.findByToken(refreshToken);
        if (stored == null) {
            throw new InvalidTokenException("Refresh token trovato");
        }

        if (stored.getExpiryDate().before(new Date())) {
            logger.warn("Token di refresh scaduto: {}", refreshToken);
            throw new RuntimeException("Refresh token expired");
        }

        User user = stored.getUser();
        String newAccessToken = jwtTokenUtil.generateAccessToken(user.getUsername());
        String newRefreshToken = jwtTokenUtil.refreshToken(user);
        refreshTokenService.createRefreshToken(user.getUsername(), newRefreshToken);
        logger.info("Nuovo token di accesso generato per l'utente: {}", user.getUsername());

        return new AuthResponse(newAccessToken, newRefreshToken, user.getUsername());
    }

    @Transactional
    public void logout(User user) {
        logger.info("Logout per l'utente: {}", user.getUsername());
        logger.info("Eliminazione token di accesso e refresh per l'utente: {}", user.getId());
        refreshTokenService.deleteByUserId(user.getId());
    }
}
