package com.myfinbank.service;

import com.myfinbank.dto.ContoDto;
import com.myfinbank.dto.RegisterRequest;
import com.myfinbank.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
public class ContoServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private ContoService contoService;

    @Autowired
    private UserRepository userRepository;

    private String email = "conto@example.com";

    @BeforeEach
    void setupUser() {
        if (!userRepository.existsByEmail(email)) {
            RegisterRequest req = new RegisterRequest();
            req.setEmail(email);
            req.setPassword("Password123");
            req.setNome("Mario");
            req.setCognome("Rossi");
            req.setCodiceFiscale("ABC123");
            req.setUsername("mariorossi");
            req.setDataNascita(LocalDate.of(1990, 1, 1));
            authService.register(req);
        }
    }

    @Test
    void testCreateAndListConti() {
        ContoDto acc = new ContoDto();
        acc.setNome("Conto Principale");
        acc.setType("CHECKING");
        acc.setCurrency("EUR");
        acc.setBalance(BigDecimal.valueOf(1000));

        ContoDto saved = contoService.createConto(email, acc);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getBalance()).isEqualByComparingTo("1000");

        List<ContoDto> conti = contoService.listConti(email);
        assertThat(conti).hasSize(1);
    }
}
