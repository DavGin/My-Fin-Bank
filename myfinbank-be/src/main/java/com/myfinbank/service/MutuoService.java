package com.myfinbank.service;


import com.myfinbank.dto.UserProfileDto;
import com.myfinbank.dto.mutuo.*;
import com.myfinbank.entity.*;
import com.myfinbank.exception.ResourceNotFoundException;
import com.myfinbank.repository.*;
import com.myfinbank.utils.StatoMutuo;
import com.myfinbank.utils.StatoRata;
import com.myfinbank.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class MutuoService {

    private static final Logger logger = LoggerFactory.getLogger(MutuoService.class);


    private final MutuoRepository mutuoRepository;
    private final UserRepository userRepository;
    private final RateCalcolateRepository rateCalcolateRepository;
    private final ContoRepository contoRepository;
    private final TransazioneRepository transazioneRepository;

    public MutuoService(MutuoRepository mutuoRepository, UserRepository userRepository,RateCalcolateRepository rateCalcolateRepository, ContoRepository contoRepository, TransazioneRepository transazioneRepository) {
        this.mutuoRepository = mutuoRepository;
        this.userRepository = userRepository;
        this.rateCalcolateRepository = rateCalcolateRepository;
        this.contoRepository = contoRepository;
        this.transazioneRepository = transazioneRepository;
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
        String numeroPratica = Util.generateRandomNumericString(8);
        mutuo.setNumeroPratica(numeroPratica);
        mutuo.setImporto(dto.getImporto());
        mutuo.setDurataMesi(dto.getDurataMesi());
        mutuo.setTassoInteresse(dto.getTassoInteresse());
        mutuo.setMotivoMutuo(dto.getMotivoMutuo());
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
    public MutuoRequestDto changeStatoMutuo(EsitoMutuoDto dto) {
        String numeroPratica = dto.getNumeroPratica();
        String newStato = dto.getNewStato();
        String motivo = dto.getMotivo();
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
//        User admin = getCurrentUser();
//        RegistroMutuo reg = new RegistroMutuo();
//        reg.setMutuo(mutuo);
//        reg.setUser(admin);
//        reg.setStato(newStato);
//        reg.setMotivo(motivo);
//        registroMutuoRepository.save(reg);



        return MutuoRequestDto.fromEntity(mutuo);
    }

    @Transactional(readOnly = true)
    public List<MutuoRequestDto> getStoricoRegistroMutuo(String numeroPratica ) {
        Mutuo mutuo = mutuoRepository.findByNumeroPratica(numeroPratica);
        if (mutuo == null) {
            throw new ResourceNotFoundException("Mutuo numero " + numeroPratica + " non trovato");
        }
        return mutuoRepository.findByMutuo(mutuo)
                .stream()
                .map(MutuoRequestDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MutuoRequestDto> getListaStoricoRegistroMutuo() {

        return mutuoRepository.findAll()
                .stream()
                .map(MutuoRequestDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RateDto> calcolaRata(String numeroPratica) {

        Mutuo mutuo = mutuoRepository.findByNumeroPratica(numeroPratica);
        if (mutuo == null) {
            throw new ResourceNotFoundException("Mutuo numero " + numeroPratica + " non trovato");
        }

//        // Controlla che il mutuo sia attivo (logica aziendale, se applicabile)
//        if (!mutuo.getStato().equals(StatoMutuo.APPROVED)) {
//            throw new IllegalStateException("Il mutuo deve essere approvato prima di calcolare le rate");
//        }

        BigDecimal quotaCapitale = mutuo.getImporto();
        int mesi = mutuo.getDurataMesi();
        BigDecimal tassoAnnuo = mutuo.getTassoInteresse().divide(BigDecimal.valueOf(100));
        BigDecimal tassoMensile = tassoAnnuo.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

        // Rata costante
        double pow = Math.pow(1 + tassoMensile.doubleValue(), -mesi);
        BigDecimal numeratore = quotaCapitale.multiply(tassoMensile);
        BigDecimal denominatore = BigDecimal.ONE.subtract(BigDecimal.valueOf(pow));
        BigDecimal pagamentoMensile = numeratore.divide(denominatore, 2, RoundingMode.HALF_UP);

        List<RateDto> rate = new ArrayList<>();
        BigDecimal saldoRimanente = quotaCapitale;
        LocalDateTime today = LocalDateTime.now();
        List<RateCalcolate> rateCalcolate = new ArrayList<>();


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

            // Stato rata
            boolean pagata =  rateCalcolateRepository.existsByMutuoAndNumeroRataAndStatoRata(mutuo, i, StatoRata.PAGATO.name());
            if (pagata) {
                inst.setStatoRata("PAGATO");
            } else if (inst.getScadenza().toLocalDate().isBefore(ChronoLocalDate.from(today))) {
                inst.setStatoRata("SCADUTO");
            } else {
                inst.setStatoRata("DA_PAGARE");
            }

            // Salvare nel database
            RateCalcolate rataCalcolata = new RateCalcolate();
            rataCalcolata.setMutuo(mutuo); // Collegamento con il mutuo
            logger.debug("Saving rata calcolata con Mutuo ID: {}", rataCalcolata.getMutuo().getId());

            rataCalcolata.setNumeroRata(i);
            rataCalcolata.setScadenza(inst.getScadenza());
            rataCalcolata.setQuotaCapitale(capitale);
            rataCalcolata.setInteressi(interessi);
            rataCalcolata.setRataTotale(pagamentoMensile);
            rataCalcolata.setSaldoRimanente(saldoRimanente);
            rataCalcolata.setStatoRata(inst.getStatoRata());

            logger.info("Salvataggio rata calcolata: ", rataCalcolata.getMutuo().getId(), i);

            rateCalcolate.add(rataCalcolata);
        }

        rateCalcolateRepository.saveAll(rateCalcolate);

        return rate;
    }




    @Transactional(readOnly = true)
    public List<RateDto> simulatoreMutuo(SimulazioneMutuoDto request) {

        BigDecimal quotaCapitale = request.getImporto();
        int mesi = request.getDurataMesi();
        BigDecimal tassoAnnuo = request.getTassoInteresse().divide(BigDecimal.valueOf(100));
        BigDecimal tassoMensile = tassoAnnuo.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

        // Rata costante
        double pow = Math.pow(1 + tassoMensile.doubleValue(), -mesi);
        BigDecimal numeratore = quotaCapitale.multiply(tassoMensile);
        BigDecimal denominatore = BigDecimal.ONE.subtract(BigDecimal.valueOf(pow));
        BigDecimal pagamentoMensile = numeratore.divide(denominatore, 2, RoundingMode.HALF_UP);

        List<RateDto> schedule = new ArrayList<>();
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

            schedule.add(inst);
        }

        return schedule;
    }

    @Transactional
    public void pagaRata(String numeroPratica, int numeroRata, String numeroConto) {
        Mutuo mutuo = mutuoRepository.findByNumeroPratica(numeroPratica);
        if (mutuo == null) {
            throw new ResourceNotFoundException("Mutuo numero " + numeroPratica + " non trovato");
        }

        // Controllo se già pagata
        boolean esiste = rateCalcolateRepository.existsByMutuoAndNumeroRataAndStatoRata(mutuo, numeroRata, StatoRata.PAGATO.name());
        if (esiste) {
            throw new IllegalStateException("Rata già pagata");
        }

//        // Calcola importo rata
//        List<RateDto> rate = calcolaRata(numeroPratica);
//        RateDto rata = rate.stream()
//                .filter(r -> r.getNumeroRata() == numeroRata)
//                .findFirst()
//                .orElseThrow(() -> new ResourceNotFoundException("Rata non trovata"));

        // Verifica saldo conto
        Conto conto = contoRepository.findByNumeroConto(numeroConto)
                .orElseThrow(() -> new ResourceNotFoundException("Conto non trovato"));

        RateCalcolate rateCalcolate = rateCalcolateRepository.findByMutuoAndNumeroRata(mutuo, numeroRata);

        if (conto.getSaldo().compareTo(rateCalcolate.getRataTotale()) < 0) {
            throw new IllegalStateException("Saldo insufficiente per pagare la rata");
        }

        // Aggiorna saldo
        conto.setSaldo(conto.getSaldo().subtract(rateCalcolate.getRataTotale()));
        contoRepository.save(conto);

        // Registra transazione
        Transazione transazione = new Transazione();
        transazione.setConto(conto);
        transazione.setImporto(rateCalcolate.getRataTotale());
        transazione.setDescrizione("Pagamento rata mutuo n." + numeroRata + " - pratica " + numeroPratica);
        transazione.setTipoTransazione("RATA_MUTUO");
        transazione.setDataTransazione(LocalDateTime.now());
        transazione.setValuta("EURO");
        transazioneRepository.save(transazione);


        rateCalcolate.setStatoRata(StatoRata.PAGATO.name());
        rateCalcolateRepository.save(rateCalcolate);

    }

    @Transactional(readOnly = true)
    public List<RateDto> findAllRatesByNumeroPraticaDto(String numeroPratica ) {
        Mutuo mutuo = mutuoRepository.findByNumeroPratica(numeroPratica);
        List<RateCalcolate> rateEntities = rateCalcolateRepository.findAllByMutuo(mutuo);
        return rateEntities.stream().map(rate -> {
            RateDto dto = new RateDto();
            dto.setNumeroRata(rate.getNumeroRata());
            dto.setQuotaCapitale(rate.getQuotaCapitale());
            dto.setInteressi(rate.getInteressi());
            dto.setRataTotale(rate.getRataTotale());
            dto.setSaldoRimanente(rate.getSaldoRimanente());
            dto.setStatoRata(rate.getStatoRata());
            dto.setScadenza(rate.getScadenza());
            return dto;
        }).collect(Collectors.toList());
    }



}
