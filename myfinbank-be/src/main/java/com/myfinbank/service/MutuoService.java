package com.myfinbank.service;


import com.myfinbank.dto.mutuo.*;
import com.myfinbank.entity.Mutuo;
import com.myfinbank.entity.RegistroMutuo;
import com.myfinbank.entity.User;
import com.myfinbank.exception.ResourceNotFoundException;
import com.myfinbank.repository.MutuoRepository;
import com.myfinbank.repository.RegistroMutuoRepository;
import com.myfinbank.repository.UserRepository;
import com.myfinbank.utils.StatoMutuo;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class MutuoService {

    private final MutuoRepository mutuoRepository;
    private final UserRepository userRepository;
    private final RegistroMutuoRepository registroMutuoRepository;

    public MutuoService(MutuoRepository mutuoRepository, UserRepository userRepository, RegistroMutuoRepository registroMutuoRepository) {
        this.mutuoRepository = mutuoRepository;
        this.userRepository = userRepository;
        this.registroMutuoRepository = registroMutuoRepository;
    }

    private User getCurrentUser() {
        String usernaem = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(usernaem)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato"));
    }

    @Transactional
    public MutuoRequestDto createMutuoRequest(MutuoRequestCreateDto dto) {
        User user = getCurrentUser();
        Mutuo mutuo = new Mutuo();
        mutuo.setUser(user);
        mutuo.setImporto(dto.getImporto());
        mutuo.setDurataMesi(dto.getDurataMesi());
        mutuo.setTassoInteresse(dto.getTassoInteresse());
        mutuo.setMotivoMutuo(dto.getMotivoMutuo());
        // status rimane PENDING di default
        mutuoRepository.save(mutuo);
        return MutuoRequestDto.fromEntity(mutuo);
    }

    @Transactional(readOnly = true)
    public List<MutuoRequestDto> listMutuiUtente() {
        User user = getCurrentUser();
        return mutuoRepository.findByUser(user)
                .stream()
                .map(MutuoRequestDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MutuoRequestDto getMutuo(String numeroPratica) {
        User user = getCurrentUser();
        Mutuo mutuo = mutuoRepository.findByNumeroPratica(numeroPratica);
        if (mutuo == null) {
            throw new ResourceNotFoundException("Mutuo numero " + numeroPratica + " non trovato");
        }
        // autorizzazione: solo proprietario o admin
        if (!mutuo.getUser().getUsername().equals(user.getUsername())) {
            // puoi creare una UnauthorizedException
            throw new SecurityException("Accesso negato alla richiesta di mutuo");
        }
        return MutuoRequestDto.fromEntity(mutuo);
    }

    @Transactional
    public MutuoRequestDto changeStatoMutuo(String numeroPratica, String newStato, String motivo) {
        Mutuo mutuo = mutuoRepository.findByNumeroPratica(numeroPratica);
        if (mutuo == null) {
            throw new ResourceNotFoundException("Mutuo numero " + numeroPratica + " non trovato");
        }
        // aggiorna stato e motivazione
        mutuo.setStato(newStato);
        if (Objects.equals(newStato, StatoMutuo.REJECTED.name())) {
            mutuo.setMotivoMutuo(motivo);
        }
        mutuoRepository.save(mutuo);

        // log decisione
        User admin = getCurrentUser();
        RegistroMutuo reg = new RegistroMutuo();
        reg.setMutuo(mutuo);
        reg.setUser(admin);
        reg.setStato(newStato);
        reg.setMotivo(motivo);
        registroMutuoRepository.save(reg);

        return MutuoRequestDto.fromEntity(mutuo);
    }

    @Transactional(readOnly = true)
    public List<RegistroMutuoDto> getStoricoRegistroMutuo(String numeroPratica ) {
        Mutuo mutuo = mutuoRepository.findByNumeroPratica(numeroPratica);
        if (mutuo == null) {
            throw new ResourceNotFoundException("Mutuo numero " + numeroPratica + " non trovato");
        }
        return registroMutuoRepository.findByMutuo(mutuo)
                .stream()
                .map(RegistroMutuoDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RateDto> calcolaRata(String numeroPratica) {
        Mutuo mutuo = mutuoRepository.findByNumeroPratica(numeroPratica);
        if (mutuo == null) {
            throw new ResourceNotFoundException("Mutuo numero " + numeroPratica + " non trovato");
        }

        BigDecimal quotaCapitale = mutuo.getImporto();
        int mesi = mutuo.getDurataMesi();
        BigDecimal tassoAnnuo = mutuo.getTassoInteresse().divide(BigDecimal.valueOf(100)); // es. 3.5% -> 0.035
        BigDecimal tassoMensile = tassoAnnuo.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

        // Rata costante
        double pow = Math.pow(1 + tassoMensile.doubleValue(), -mesi);
        BigDecimal numeratore = quotaCapitale.multiply(tassoMensile);
        BigDecimal denominatore = BigDecimal.ONE.subtract(BigDecimal.valueOf(pow));
        BigDecimal pagamentoMensile = numeratore.divide(denominatore, 2, RoundingMode.HALF_UP);

        List<RateDto> rate = new ArrayList<>();
        BigDecimal saldoRimanente = quotaCapitale;

        for (int i = 1; i <= mesi; i++) {
            BigDecimal interessi = saldoRimanente.multiply(tassoMensile).setScale(2, RoundingMode.HALF_UP);
            BigDecimal capitale = pagamentoMensile.subtract(interessi).setScale(2, RoundingMode.HALF_UP);
            saldoRimanente = saldoRimanente.subtract(capitale).setScale(2, RoundingMode.HALF_UP);

            RateDto inst = new RateDto();
            inst.setNumeroRata(i);
            inst.setScadenza(LocalDateTime.now().plusMonths(i));
            inst.setQuotaCapitale(capitale);
            inst.setInteressi(interessi);
            inst.setRataTotale(pagamentoMensile);
            inst.setSaldoRimanente(saldoRimanente.max(BigDecimal.ZERO));

            rate.add(inst);
        }

        return rate;
    }


    @Transactional(readOnly = true)
    public List<RateDto> simulatoreMutuo(SimulazioneMutuoDto request) {
        BigDecimal quotaCapitale = request.getImporto();
        int mesi = request.getDurateMesi();
        BigDecimal interessiAnnuali = request.getTassoInteresse().divide(BigDecimal.valueOf(100));
        BigDecimal interessiMensili = interessiAnnuali.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

        double pow = Math.pow(1 + interessiMensili.doubleValue(), -mesi);
        BigDecimal numeratore = quotaCapitale.multiply(interessiMensili);
        BigDecimal denominatore = BigDecimal.ONE.subtract(BigDecimal.valueOf(pow));
        BigDecimal rataMensile = numeratore.divide(denominatore, 2, RoundingMode.HALF_UP);

        List<RateDto> schedule = new ArrayList<>();
        BigDecimal saldoRimanente = quotaCapitale;

        for (int i = 1; i <= mesi; i++) {
            BigDecimal interessi = saldoRimanente.multiply(rataMensile).setScale(2, RoundingMode.HALF_UP);
            BigDecimal capitale = interessiMensili.subtract(interessi).setScale(2, RoundingMode.HALF_UP);
            saldoRimanente = saldoRimanente.subtract(capitale).setScale(2, RoundingMode.HALF_UP);

            RateDto inst = new RateDto();
            inst.setNumeroRata(i);
            inst.setScadenza(LocalDateTime.now().plusMonths(i));
            inst.setQuotaCapitale(capitale);
            inst.setInteressi(interessi);
            inst.setRataTotale(rataMensile);
            inst.setSaldoRimanente(saldoRimanente.max(BigDecimal.ZERO));

            schedule.add(inst);
        }

        return schedule;
    }


}
