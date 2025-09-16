package com.myfinbank.service;

import com.myfinbank.dto.TransazioneDto;
import com.myfinbank.entity.Conto;
import com.myfinbank.entity.Transazione;
import com.myfinbank.exception.ResourceNotFoundException;
import com.myfinbank.repository.ContoRepository;
import com.myfinbank.repository.TransazioneRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransazioneService {

    private static final Logger logger = LoggerFactory.getLogger(TransazioneService.class);


    private final TransazioneRepository transazioneRepository;
    private final ContoRepository contoRepository;

    public TransazioneService(TransazioneRepository transazioneRepository, ContoRepository contoRepository) {
        this.transazioneRepository = transazioneRepository;
        this.contoRepository = contoRepository;
    }

    private String getCurrentUserUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private void validateAccountOwnership(Conto conto) {
        String username = getCurrentUserUsername();
        if (!conto.getUser().getUsername().equals(username)) {
            throw new SecurityException("Accesso negato: il conto non appartiene all’utente loggato");
        }
    }

    @Transactional
    public TransazioneDto creaTransazione(String numeroConto, TransazioneDto dto) {
        Conto sorgente = contoRepository.findByNumeroConto(numeroConto)
                .orElseThrow(() -> new ResourceNotFoundException("Conto " + numeroConto + " non trovato"));

        validateAccountOwnership(sorgente);

        Transazione tx = new Transazione();
        tx.setConto(sorgente);
        tx.setTipoTransazione(dto.getTipoTransazione());
        tx.setImporto(dto.getImporto());
        tx.setValuta(dto.getValuta());
        tx.setDescrizione(dto.getDescrizione());

        if ("DEPOSITO".equalsIgnoreCase(dto.getTipoTransazione())) {
            sorgente.setSaldo(sorgente.getSaldo().add(dto.getImporto()));
            contoRepository.save(sorgente);
        }
        else if ("PRELIEVO".equalsIgnoreCase(dto.getTipoTransazione())) {
            if (sorgente.getSaldo().compareTo(dto.getImporto()) < 0) {
                throw new IllegalArgumentException("Saldo insufficiente per prelievo");
            }
            sorgente.setSaldo(sorgente.getSaldo().subtract(dto.getImporto()));
            contoRepository.save(sorgente);
        }
        else if ("BONIFICO".equalsIgnoreCase(dto.getTipoTransazione())) {
            if (dto.getTargetIban() == null) {
                throw new IllegalArgumentException("Per un trasferimento è richiesto l'IBAN del destinatario");
            }

            Conto target = contoRepository.findByIban(dto.getTargetIban())
                    .orElseThrow(() -> new ResourceNotFoundException("Conto con " + dto.getTargetIban() + " non trovato"));

            if (sorgente.getSaldo().compareTo(dto.getImporto()) < 0) {
                throw new IllegalArgumentException("Saldo insufficiente per bonifico");
            }

            if (!sorgente.getUser().getId().equals(target.getUser().getId())) {
                logger.info("Bonifico inter-utente: {} -> {} | importo: {} {}",
                        sorgente.getUser().getUsername(),
                        target.getUser().getUsername(),
                        dto.getImporto(),
                        dto.getValuta());
            }

            // Aggiorna saldi
            sorgente.setSaldo(sorgente.getSaldo().subtract(dto.getImporto()));
            target.setSaldo(target.getSaldo().add(dto.getImporto()));

            contoRepository.save(sorgente);
            contoRepository.save(target);

            // Registra transazione anche lato destinatario
            Transazione inEntrata = new Transazione();
            inEntrata.setConto(target);
            inEntrata.setTipoTransazione("ENTRATA");
            inEntrata.setImporto(dto.getImporto());
            inEntrata.setValuta(dto.getValuta());
            inEntrata.setDescrizione("Bonifico ricevuto da " + sorgente.getUser().getNome() + " " + sorgente.getUser().getCognome()  + " per " + dto.getDescrizione());
            transazioneRepository.save(inEntrata);
        }

        transazioneRepository.save(tx);

        return TransazioneDto.fromEntity(tx);
    }

    @Transactional(readOnly = true)
    public List<TransazioneDto> listTransazioni(String numeroConto) {
        Conto conto = contoRepository.findByNumeroConto(numeroConto)
                .orElseThrow(() -> new ResourceNotFoundException("Conto " + numeroConto + " non trovato"));

        validateAccountOwnership(conto);

        return transazioneRepository.findByConto(conto)
                .stream()
                .map(TransazioneDto::fromEntity)
                .collect(Collectors.toList());
    }
}
