package com.myfinbank.service;

import com.myfinbank.dto.ContoDto;
import com.myfinbank.entity.Conto;
import com.myfinbank.entity.User;
import com.myfinbank.exception.ResourceNotFoundException;
import com.myfinbank.repository.ContoRepository;
import com.myfinbank.repository.UserRepository;
import com.myfinbank.utils.Util;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContoService {

    private final ContoRepository contoRepository;
    private final UserRepository userRepository;

    public ContoService(ContoRepository contoRepository, UserRepository userRepository) {
        this.contoRepository = contoRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ContoDto createConto(String username, ContoDto dto) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato: " + username + ""));
        Conto conto = new Conto();
        conto.setUser(user);
        conto.setNumeroConto(dto.getNumeroConto());
        conto.setTipo(dto.getTipo());
        conto.setIban(Util.generateIban("IT", "12345", "67890", dto.getNumeroConto()));
        conto.setValuta(dto.getValuta());
        conto.setSaldo(dto.getSaldo() != null ? dto.getSaldo() : conto.getSaldo());
        contoRepository.save(conto);
        return ContoDto.fromEntity(conto);
    }

    @Transactional(readOnly = true)
    public List<ContoDto> listConti(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato: " + username + ""));
        return contoRepository.findByUser(user)
                .stream().map(ContoDto::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ContoDto findByNumeroConto(String numeroConto) {
        Conto conto = contoRepository.findByNumeroConto(numeroConto)
                .orElseThrow(() -> new ResourceNotFoundException("Conto non trovato con numero: " + numeroConto));
        return ContoDto.fromEntity(conto);
    }

    @Transactional
    public void chiudiConto(String numeroConto) {
        Conto conto = contoRepository.findByNumeroConto(numeroConto)
                .orElseThrow(() -> new ResourceNotFoundException("Conto non trovato con numero: " + numeroConto));

        if (conto.getDataChiusura() != null) {
            throw new IllegalStateException("Il conto è già stato chiuso.");
        }

        conto.setDataChiusura(LocalDateTime.now());
        conto.setSaldo(BigDecimal.ZERO); // Azzeriamo il saldo se richiesto dalla logica.
        contoRepository.save(conto);
    }


}
