package com.myfinbank.service;

import com.myfinbank.dto.ContoDto;
import com.myfinbank.dto.RegisterRequest;
import com.myfinbank.dto.TransazioneDto;
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
class TransactionServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private ContoService contoService;

    @Autowired
    private TransazioneService transazioneService;

    @Autowired
    private UserRepository userRepository;

    private String email = "txuser@example.com";
    private String username = "lucabianchi";
    private String numeroConto= "00000000000";

    @BeforeEach
    void setup() {
        if (!userRepository.existsByEmail(email)) {
            RegisterRequest req = new RegisterRequest();
            req.setEmail(email);
            req.setPassword("Password123");
            req.setUsername("lucabianchi");
            req.setEmail("test@example.com");
            req.setPassword("Password123");
            req.setNome("Luca");
            req.setCognome("Bianchi");
            req.setCodiceFiscale("ABC123");
            req.setDataNascita(LocalDate.of(1990, 1, 1));
            authService.register(req);
        }

        ContoDto conto = new ContoDto();
        conto.setNumeroConto("123456789");
        conto.setIban("IT123456789012345678901234");
        conto.setTipo("CHECKING");
        conto.setValuta("EUR");
        conto.setSaldo(BigDecimal.valueOf(500));


        ContoDto saved = contoService.createConto(username, conto);
        numeroConto = saved.getNumeroConto();
    }

    @Test
    void testCreateAndListTransactions() {
        TransazioneDto deposito = new TransazioneDto();
        deposito.setTipoTransazione("DEPOSITO");
        deposito.setImporto(BigDecimal.valueOf(200));
        deposito.setValuta("EUR");
        deposito.setDescrizione("Versamento stipendio");


        TransazioneDto saved = transazioneService.creaTransazione(deposito);

        assertThat(saved.getId()).isNotNull();

        List<TransazioneDto> txs = transazioneService.listTransazioni(numeroConto);
        assertThat(txs).hasSize(1);
        assertThat(txs.get(0).getDescrizione()).isEqualTo("Versamento stipendio");
    }

    @Test
    void testTransferBetweenAccounts() {
        // Crea un secondo conto per il destinatario
        ContoDto conto2 = new ContoDto();
        conto2.setNumeroConto("1234567890");
        conto2.setTipo("CHECKING");
        conto2.setIban("IT123456789012345678901235");
        conto2.setValuta("EUR");
        conto2.setSaldo(BigDecimal.valueOf(300));
        ContoDto savedConto2 = contoService.createConto(username, conto2);

        // Effettua bonifico 100€ da acc principale a acc2
        TransazioneDto tx = new TransazioneDto();
        tx.setTipoTransazione("BONIFICO");
        tx.setImporto(BigDecimal.valueOf(100));
        tx.setValuta("EUR");
        tx.setDescrizione("Bonifico verso conto2");
        tx.setTargetIban(savedConto2.getIban());

        TransazioneDto savedTx = transazioneService.creaTransazione(tx);

        assertThat(savedTx.getId()).isNotNull();

        // Verifica saldi aggiornati
        List<ContoDto> conti = contoService.listConti(username);
        ContoDto updatedSource = conti.stream()
                .filter(a -> a.getNumeroConto().equals(numeroConto))
                .findFirst().orElseThrow();
        ContoDto updatedTarget = conti.stream()
                .filter(a -> a.getNumeroConto().equals(savedConto2.getNumeroConto()))
                .findFirst().orElseThrow();

        assertThat(updatedSource.getSaldo()).isEqualByComparingTo("400");
        assertThat(updatedTarget.getSaldo()).isEqualByComparingTo("400");
    }


    @Test
    void testTransferBetweenDifferentUsers() {
        // Creo un altro utente
        RegisterRequest req = new RegisterRequest();
        req.setEmail("other@example.com");
        req.setPassword("Secret123");
        req.setUsername("mariobianchi");
        req.setNome("Mario");
        req.setCognome("Bianchi");
        req.setCodiceFiscale("ABC1234");
        req.setDataNascita(LocalDate.of(1990, 1, 1));
        authService.register(req);

        // Creo conto destinatario
        ContoDto conto3 = new ContoDto();
        conto3.setNumeroConto("");
        conto3.setTipo("CHECKING");
        conto3.setIban("IT123456789012345678901235");
        conto3.setValuta("EUR");
        conto3.setSaldo(BigDecimal.valueOf(300));
        ContoDto marioConto = contoService.createConto("mariobianchi", conto3);

        // Bonifico 100€ verso Mario
        TransazioneDto tx = new TransazioneDto();
        tx.setTipoTransazione("BONIFICO");
        tx.setImporto(BigDecimal.valueOf(100));
        tx.setValuta("EUR");
        tx.setDescrizione("Bonifico a Mario");
        tx.setTargetIban(marioConto.getIban());

        TransazioneDto savedTx = transazioneService.creaTransazione(tx);

        assertThat(savedTx.getId()).isNotNull();

        // Verifica saldi aggiornati
        ContoDto updatedSource = contoService.listConti(username).stream()
                .filter(a -> a.getNumeroConto().equals(numeroConto))
                .findFirst().orElseThrow();

        ContoDto updatedTarget = contoService.listConti("mariobianchi").stream()
                .filter(a -> a.getNumeroConto().equals(marioConto.getNumeroConto()))
                .findFirst().orElseThrow();

        assertThat(updatedSource.getSaldo()).isEqualByComparingTo("400");
        assertThat(updatedTarget.getSaldo()).isEqualByComparingTo("400");
    }
}
