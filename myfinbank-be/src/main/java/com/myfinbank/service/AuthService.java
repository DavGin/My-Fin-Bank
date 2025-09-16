package com.myfinbank.service;

import com.myfinbank.controller.AuthController;
import com.myfinbank.dto.*;
import com.myfinbank.entity.RefreshToken;
import com.myfinbank.entity.User;
import com.myfinbank.repository.UserRepository;
import com.myfinbank.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider jwtTokenProvider,
                       RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public void register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            logger.warn("Registrazione fallita: email già in uso - {}", request.getEmail());
            throw new IllegalArgumentException("Email già in uso: " + request.getEmail() + "");
        }

        User u = new User();

        u.setUsername(request.getUsername());
        u.setEmail(request.getEmail());
        u.setPassword(passwordEncoder.encode(request.getPassword()));
        u.setNome(request.getNome());
        u.setCognome(request.getCognome());
        u.setCodiceFiscale(request.getCodiceFiscale());
        u.setDataNascita(request.getDataNascita());
        u.setRuolo("USER");
        userRepository.save(u);

    }

    @Transactional
    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        String accessToken = jwtTokenProvider.generateAccessToken(request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalStateException("Utente autenticato non trovato"
                ));

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return new AuthResponse(accessToken, refreshToken.getToken(), user.getEmail());
    }

    @Transactional
    public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {

        String requestRefreshToken = request.getRefreshToken();

        RefreshTokenService rts = this.refreshTokenService;
        Optional<RefreshToken> maybeToken = rts.findByToken(requestRefreshToken);

        RefreshToken refreshToken = maybeToken.orElseThrow(() -> new IllegalArgumentException("Refresh token non trovato"));
        rts.verifyExpiration(refreshToken);

        Long userId = refreshToken.getUser().getId();
        rts.deleteByUserId(userId);

        RefreshToken newRefreshToken = rts.createRefreshToken(userId);

        String username = refreshToken.getUser().getEmail();
        String newAccessToken = jwtTokenProvider.generateAccessToken(username);

        return new TokenRefreshResponse(newAccessToken, newRefreshToken.getToken());
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
