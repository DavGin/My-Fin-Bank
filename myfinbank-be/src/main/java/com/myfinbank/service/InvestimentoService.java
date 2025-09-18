package com.myfinbank.service;

import com.myfinbank.dto.investimento.CreaInvestimentoDto;
import com.myfinbank.dto.investimento.InvestimentoDto;
import com.myfinbank.entity.Investimento;
import com.myfinbank.entity.User;
import com.myfinbank.exception.ResourceNotFoundException;
import com.myfinbank.repository.InvestimentoRepository;
import com.myfinbank.repository.UserRepository;
import com.myfinbank.utils.StatoInvestimento;
import com.myfinbank.utils.Util;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InvestimentoService {

    private final InvestimentoRepository investimentoRepository;
    private final UserRepository userRepository;

    public InvestimentoService(InvestimentoRepository investimentoRepository, UserRepository userRepository) {
        this.investimentoRepository = investimentoRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public InvestimentoDto createInvestment(CreaInvestimentoDto dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User " + username + "non trovato"));

        Investimento inv = new Investimento();
        inv.setUser(user);
        inv.setIdentificativo(Util.generateRandomNumericString(8));
        inv.setTipoInvestimento(dto.getTipoInvestimento());
        inv.setImportoInvestito(dto.getImportoInvestito());
        inv.setTassoRitornoPrevisto(dto.getTassoRitornoPrevisto());
        inv.setStatoInvestimento(StatoInvestimento.ACTIVE.name());
        inv.setDataInizio(LocalDateTime.now());

        return InvestimentoDto.fromEntity(investimentoRepository.save(inv));
    }

    @Transactional(readOnly = true)
    public List<InvestimentoDto> getListaInvestimentiAttivi () {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User " + username + "non trovato"));

        return investimentoRepository.findByUser(user)
                .stream()
                .map(InvestimentoDto::fromEntity)
                .toList();
    }

    @Transactional
    public InvestimentoDto chiudiInvestimento(String identificativo) {
        Investimento inv = investimentoRepository.findByIdentificativo(identificativo)
                .orElseThrow(() -> new ResourceNotFoundException("Investimento " +  identificativo + "non trovato"));
        inv.setStatoInvestimento(StatoInvestimento.CLOSED.name());
        inv.setDataFine(LocalDateTime.now());
        return InvestimentoDto.fromEntity(investimentoRepository.save(inv));
    }
}
