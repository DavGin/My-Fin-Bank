package com.myfinbank.service;

import com.myfinbank.dto.*;
import com.myfinbank.entity.RefreshToken;
import com.myfinbank.entity.User;
import com.myfinbank.repository.UserRepository;
import com.myfinbank.security.JwtTokenProvider;
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

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenUtil jwtTokenUtil;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider jwtTokenProvider,
                       RefreshTokenService refreshTokenService,
                       JwtTokenUtil jwtTokenUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenService = refreshTokenService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Transactional
    public void register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            logger.warn("Registrazione fallita: email già in uso - {}", request.getEmail());
            throw new IllegalArgumentException("Email già in uso: " + request.getEmail() + "");
        }

        User u = new User();

        String ruolo = String.valueOf(Ruoli.USER);
        if(ruolo == null) {
            throw new IllegalStateException("Ruolo non trovato");
        }

        u.setUsername(request.getUsername());
        u.setEmail(request.getEmail());
        u.setPassword(passwordEncoder.encode(request.getPassword()));
        u.setNome(request.getNome());
        u.setCognome(request.getCognome());
        u.setCodiceFiscale(request.getCodiceFiscale());
        u.setDataNascita(request.getDataNascita());
        u.setRuolo(ruolo);
        userRepository.save(u);

    }

    public AuthResponse login(AuthRequest request) {
        // valida credenziali...
        User user = userRepository.findByUsername(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));
        long expirationMillis = Duration.ofMinutes(15).toMillis();

        String accessToken = jwtTokenUtil.generateToken(user.getUsername(), user.getRuolo());
        String refreshToken = jwtTokenUtil.refreshToken(user); // 7 giorni

        refreshTokenService.createRefreshToken(user.getUsername());

        return new AuthResponse(accessToken, refreshToken, user.getUsername());
    }

    public AuthResponse refreshAccessToken(String refreshToken) {
        RefreshToken stored = refreshTokenService.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (stored.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token expired");
        }

        User user = stored.getUser();
        String newAccessToken = jwtTokenUtil.generateToken(user.getUsername(), user.getRuolo());

        return new AuthResponse(refreshToken, newAccessToken, user.getUsername());
    }

    @Transactional
    public void logout(String refreshTokenString) {
        if (refreshTokenString == null || refreshTokenString.isBlank()) {
            logger.warn("Logout fallito: token di refresh assente o vuoto.");
            return;
        }
        Optional<RefreshToken> maybeToken = refreshTokenService.findByToken(refreshTokenString);
        maybeToken.ifPresent(rt -> {
            refreshTokenService.deleteByUserId(rt.getUser().getId());
        });
    }
}
