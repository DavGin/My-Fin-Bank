package com.myfinbank.service;

import com.myfinbank.dto.investimento.*;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
        inv.setDataFine(LocalDateTime.now().plusMonths(dto.getDurataMesi()));
        inv.setMesi(dto.getDurataMesi());


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
                .orElseThrow(() -> new ResourceNotFoundException("Investimento " +  identificativo + " non trovato"));
        inv.setStatoInvestimento(StatoInvestimento.CLOSED.name());
        inv.setDataFine(LocalDateTime.now());
        return InvestimentoDto.fromEntity(investimentoRepository.save(inv));
    }

    @Transactional(readOnly = true)
    public ProiezioneInvestimentoDto proiezioneInvestimento(String identificativo, int mesi) {
        Investimento inv = investimentoRepository.findByIdentificativo(identificativo)
                .orElseThrow(() -> new ResourceNotFoundException("Investimento " + identificativo + " non trovato"));

        BigDecimal iniziale = inv.getImportoInvestito();
        BigDecimal tasso = inv.getTassoRitornoPrevisto().divide(BigDecimal.valueOf(100)); // es. 5% → 0.05

        BigDecimal importoFinale = iniziale.multiply(
                BigDecimal.valueOf(Math.pow(1 + tasso.doubleValue(), mesi))
        ).setScale(2, RoundingMode.HALF_UP);

        ProiezioneInvestimentoDto dto = ProiezioneInvestimentoDto.fromEntity(inv);
        dto.setImportoTotale(importoFinale);
        return dto;

    }

    @Transactional(readOnly = true)
    public SimulazioneInvestimentoOutputDto simulaInvestimento(SimulazioneInvestimentoDto request) {
        BigDecimal importoIniziale = request.getImportoIniziale();
        BigDecimal rate = request.getTassoPrevisto().divide(BigDecimal.valueOf(100));
        int mesi = request.getMesi();

        BigDecimal importoFinale = importoIniziale.multiply(
                BigDecimal.valueOf(Math.pow(1 + rate.doubleValue(), mesi))
        ).setScale(2, RoundingMode.HALF_UP);

        SimulazioneInvestimentoOutputDto dto = new SimulazioneInvestimentoOutputDto();
        dto.setImportoIniziale(importoIniziale);
        dto.setTassoPrevisto(request.getTassoPrevisto());
        dto.setMesi(mesi);
        dto.setImportoFinale(importoFinale);

        return dto;
    }

    @Transactional(readOnly = true)
    public List<RendimentoInvestimentoDto> getStoricoRendimenti(String numeroIdentificativo ) {
        Investimento inv = investimentoRepository.findByIdentificativo(numeroIdentificativo)
                .orElseThrow(() -> new ResourceNotFoundException("Investimento " + numeroIdentificativo + " non trovato"));

        BigDecimal importo = inv.getImportoInvestito();
        BigDecimal tassoMensile = inv.getTassoRitornoPrevisto()
                .divide(BigDecimal.valueOf(100 * 12), 10, RoundingMode.HALF_UP);
        int mesi = inv.getMesi();

        List<RendimentoInvestimentoDto> storico = new ArrayList<>();
        BigDecimal valoreAttuale = importo;

        for (int i = 1; i <= mesi; i++) {
            BigDecimal rendimento = valoreAttuale.multiply(tassoMensile).setScale(2, RoundingMode.HALF_UP);
            BigDecimal nuovoValore = valoreAttuale.add(rendimento).setScale(2, RoundingMode.HALF_UP);

            String periodo = String.format("%d-%02d",
                    inv.getDataInizio().getYear(),
                    inv.getDataInizio().getMonthValue() + i > 12
                            ? (inv.getDataInizio().getYear() + (inv.getDataInizio().getMonthValue() + i - 1) / 12)
                            : inv.getDataInizio().getYear()
            ) + "-" +
                    String.format("%02d", ((inv.getDataInizio().getMonthValue() + i - 1) % 12) + 1);

            storico.add(new RendimentoInvestimentoDto(periodo, valoreAttuale, rendimento, nuovoValore));

            valoreAttuale = nuovoValore;
        }

        return storico;
    }
}

