package com.myfinbank.service;

import com.myfinbank.dto.AuthRequest;
import com.myfinbank.dto.AuthResponse;
import com.myfinbank.dto.RegisterRequest;
import com.myfinbank.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testRegisterAndLogin() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("mariorossi");
        req.setEmail("test@example.com");
        req.setPassword("Password123");
        req.setNome("Mario");
        req.setCognome("Rossi");
        req.setCodiceFiscale("ABC123");
        req.setDataNascita(LocalDate.of(1990, 1, 1));

        authService.register(req);

        assertThat(userRepository.existsByEmail("test@example.com")).isTrue();

        AuthRequest login = new AuthRequest();
        login.setUsername("mariorossi");
        login.setPassword("Password123");

        AuthResponse resp = authService.login(login);
        assertThat(resp.getAccessToken()).isNotBlank();
        assertThat(resp.getRefreshToken()).isNotBlank();
    }
}
