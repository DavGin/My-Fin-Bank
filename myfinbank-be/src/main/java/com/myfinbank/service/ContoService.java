package com.myfinbank.service;

import com.myfinbank.dto.ContoDto;
import com.myfinbank.entity.Conto;
import com.myfinbank.entity.User;
import com.myfinbank.exception.ResourceNotFoundException;
import com.myfinbank.repository.ContoRepository;
import com.myfinbank.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        Conto acc = new Conto();
        acc.setUser(user);
        acc.setNome(dto.getNome());
        acc.setType(dto.getType());
        acc.setCurrency(dto.getCurrency());
        acc.setBalance(dto.getBalance() != null ? dto.getBalance() : acc.getBalance());
        contoRepository.save(acc);
        return ContoDto.fromEntity(acc);
    }

    @Transactional(readOnly = true)
    public List<ContoDto> listConti(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato: " + username + ""));
        return contoRepository.findByUser(user)
                .stream().map(ContoDto::fromEntity).collect(Collectors.toList());
    }

}
