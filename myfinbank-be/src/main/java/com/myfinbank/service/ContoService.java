package com.myfinbank.service;

import com.myfinbank.dto.ContoDto;
import com.myfinbank.entity.Conto;
import com.myfinbank.entity.User;
import com.myfinbank.exception.ResourceNotFoundException;
import com.myfinbank.repository.ContoRepository;
import com.myfinbank.repository.UserRepository;
import com.myfinbank.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContoService {

    private static final Logger logger = LoggerFactory.getLogger(ContoService.class);

    private final ContoRepository contoRepository;
    private final UserRepository userRepository;

    public ContoService(ContoRepository contoRepository, UserRepository userRepository) {
        this.contoRepository = contoRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ContoDto createConto(String username, ContoDto dto) {
        logger.info("Avvio creazione del conto per l'utente: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("Utente non trovato per username: {}", username);
                    return new ResourceNotFoundException("Utente non trovato: " + username);
                });

        logger.debug("Recuperato utente: {}", user);
        String numeroConto = Util.generateRandomNumericString(10);
        Conto conto = new Conto();
        conto.setUser(user);
        conto.setNumeroConto(numeroConto);
        conto.setTipo(dto.getTipo());
        conto.setIban(Util.generateIban("IT", "12345", "67890", numeroConto));
        conto.setValuta(dto.getValuta());
        conto.setSaldo(dto.getSaldo() != null ? dto.getSaldo() : BigDecimal.ZERO);

        contoRepository.save(conto);

        logger.info("Conto creato con successo per l'utente: {}, Numero Conto: {}", username, dto.getNumeroConto());
        return ContoDto.fromEntity(conto);
    }

    @Transactional(readOnly = true)
    public List<ContoDto> listConti(String username) {
        logger.info("Caricamento lista dei conti per l'utente: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("Utente non trovato per username: {}", username);
                    return new ResourceNotFoundException("Utente non trovato: " + username);
                });

        logger.debug("Recuperato utente: {}", user);

        List<ContoDto> conti = contoRepository.findByUser(user)
                .stream().map(ContoDto::fromEntity).collect(Collectors.toList());

        logger.info("Trovati {} conti per l'utente: {}", conti.size(), username);
        return conti;
    }

    @Transactional(readOnly = true)
    public ContoDto findByNumeroConto(String numeroConto) {
        logger.info("Ricerca conto per NumeroConto: {}", numeroConto);

        Conto conto = contoRepository.findByNumeroConto(numeroConto)
                .orElseThrow(() -> {
                    logger.warn("Conto non trovato per NumeroConto: {}", numeroConto);
                    return new ResourceNotFoundException("Conto non trovato con numero: " + numeroConto);
                });

        logger.info("Conto trovato per NumeroConto: {}", numeroConto);
        return ContoDto.fromEntity(conto);
    }

    @Transactional
    public void chiudiConto(String numeroConto) {
        logger.info("Avvio chiusura del conto per NumeroConto: {}", numeroConto);

        Conto conto = contoRepository.findByNumeroConto(numeroConto)
                .orElseThrow(() -> {
                    logger.warn("Conto non trovato per NumeroConto: {}", numeroConto);
                    return new ResourceNotFoundException("Conto non trovato con numero: " + numeroConto);
                });

        if (conto.getDataChiusura() != null) {
            logger.error("Tentativo di chiudere un conto già chiuso. NumeroConto: {}", numeroConto);
            throw new IllegalStateException("Il conto è già stato chiuso.");
        }

        conto.setDataChiusura(LocalDateTime.now());
        conto.setSaldo(BigDecimal.ZERO); // Azzeriamo il saldo se richiesto dalla logica.
        contoRepository.save(conto);

        logger.info("Conto chiuso con successo per NumeroConto: {}", numeroConto);
    }
}
