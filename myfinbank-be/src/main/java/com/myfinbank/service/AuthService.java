package com.myfinbank.service;

import com.myfinbank.dto.*;
import com.myfinbank.entity.RefreshToken;
import com.myfinbank.entity.User;
import com.myfinbank.repository.UserRepository;
import com.myfinbank.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

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
            throw new IllegalArgumentException("Email already in use");
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
        // Authenticate credentials
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // If authentication is successful, create access token and refresh token
        String accessToken = jwtTokenProvider.generateAccessToken(request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return new AuthResponse(accessToken, refreshToken.getToken(), user.getEmail());
    }

    @Transactional
    public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        RefreshTokenService rts = this.refreshTokenService;
        Optional<RefreshToken> maybeToken = rts.findByToken(requestRefreshToken);

        RefreshToken refreshToken = maybeToken.orElseThrow(() -> new IllegalArgumentException("Refresh token not found"));
        // check expiration
        rts.verifyExpiration(refreshToken);

        // rotate: delete old and create new refresh token (or update)
        Long userId = refreshToken.getUser().getId();
        // delete old token
        // we already have token entity; delete single record:
        // (we could delete or keep; here we'll delete and create a new one)
        // delete by entity:
        // refreshTokenRepository.delete(refreshToken); -> not exposed here, so:
        // use deleteByUserId to remove all tokens for this user, then create a fresh one.
        rts.deleteByUserId(userId);

        RefreshToken newRefreshToken = rts.createRefreshToken(userId);

        String username = refreshToken.getUser().getEmail();
        String newAccessToken = jwtTokenProvider.generateAccessToken(username);

        return new TokenRefreshResponse(newAccessToken, newRefreshToken.getToken());
    }

    @Transactional
    public void logout(String refreshTokenString) {
        if (refreshTokenString == null || refreshTokenString.isBlank()) return;
        Optional<RefreshToken> maybeToken = refreshTokenService.findByToken(refreshTokenString);
        maybeToken.ifPresent(rt -> refreshTokenService.deleteByUserId(rt.getUser().getId()));
    }
}
