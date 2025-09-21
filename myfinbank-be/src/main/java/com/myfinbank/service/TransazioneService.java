package com.myfinbank.service;

import com.myfinbank.dto.TransazioneDto;
import com.myfinbank.entity.Conto;
import com.myfinbank.entity.Transazione;
import com.myfinbank.exception.ResourceNotFoundException;
import com.myfinbank.repository.ContoRepository;
import com.myfinbank.repository.TransazioneRepository;
import com.myfinbank.utils.DirezioneTransazione;
import com.myfinbank.utils.TipoTransazione;
import com.myfinbank.utils.Util;
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
    public TransazioneDto creaTransazione(TransazioneDto dto) {
        String numeroConto = dto.getNumeroConto();
        logger.info("Avvio della creazione di una transazione per il conto: {}", numeroConto);
        Conto sorgente = contoRepository.findByNumeroConto(numeroConto)
                .orElseThrow(() -> {
                    logger.error("Conto non trovato: {}", numeroConto);
                    return new ResourceNotFoundException("Conto " + numeroConto + " non trovato");
                });

        logger.debug("Conto sorgente recuperato: {} | Saldo attuale: {}", numeroConto, sorgente.getSaldo());

        validateAccountOwnership(sorgente);
        logger.info("Proprietà del conto validata per l'utente: {}", getCurrentUserUsername());

        Transazione tx = new Transazione();
        tx.setConto(sorgente);
        tx.setTipoTransazione(dto.getTipoTransazione());
        tx.setImporto(dto.getImporto());
        tx.setValuta(dto.getValuta());
        tx.setDescrizione(dto.getDescrizione());

        logger.info("Dettagli della transazione: Tipo: {}, Importo: {}, Valuta: {}",
                dto.getTipoTransazione(), dto.getImporto(), dto.getValuta());

        if (TipoTransazione.DEPOSITO.name().equalsIgnoreCase(dto.getTipoTransazione())) {
            sorgente.setSaldo(sorgente.getSaldo().add(dto.getImporto()));
            tx.setDirezione(DirezioneTransazione.ENTRATA.name());
            logger.info("Deposito effettuato. Nuovo saldo: {}", sorgente.getSaldo());
            contoRepository.save(sorgente);
        } else if (TipoTransazione.PRELIEVO.name().equalsIgnoreCase(dto.getTipoTransazione())) {
            if (sorgente.getSaldo().compareTo(dto.getImporto()) < 0) {
                logger.error("Saldo insufficiente per prelievo. Saldo attuale: {}, Importo richiesto: {}",
                        sorgente.getSaldo(), dto.getImporto());
                tx.setDirezione(DirezioneTransazione.USCITA.name());
                throw new IllegalArgumentException("Saldo insufficiente per prelievo");
            }
            sorgente.setSaldo(sorgente.getSaldo().subtract(dto.getImporto()));
            logger.info("Prelievo effettuato. Nuovo saldo: {}", sorgente.getSaldo());
            contoRepository.save(sorgente);
        } else if (TipoTransazione.BONIFICO.name().equalsIgnoreCase(dto.getTipoTransazione()) || TipoTransazione.PAGAMENTO.name().equalsIgnoreCase(dto.getTipoTransazione())) {
            if (dto.getTargetIban() == null) {
                logger.error("IBAN del destinatario mancante per il bonifico.");
                tx.setDirezione(DirezioneTransazione.USCITA.name());
                throw new IllegalArgumentException("Per un trasferimento è richiesto l'IBAN del destinatario");
            }

            Conto target = contoRepository.findByIban(dto.getTargetIban())
                    .orElseThrow(() -> {
                        logger.error("Conto destinatario non trovato per IBAN: {}", dto.getTargetIban());
                        return new ResourceNotFoundException("Conto con " + dto.getTargetIban() + " non trovato");
                    });

            if (sorgente.getSaldo().compareTo(dto.getImporto()) < 0) {
                logger.error("Saldo insufficiente per bonifico. Saldo attuale: {}, Importo: {}",
                        sorgente.getSaldo(), dto.getImporto());
                throw new IllegalArgumentException("Saldo insufficiente per bonifico");
            }

            if (!sorgente.getUser().getId().equals(target.getUser().getId())) {
                logger.info("Bonifico inter-utente: {} -> {} | Importo: {} {}",
                        sorgente.getUser().getUsername(),
                        target.getUser().getUsername(),
                        dto.getImporto(),
                        dto.getValuta());
            }

            sorgente.setSaldo(sorgente.getSaldo().subtract(dto.getImporto()));
            target.setSaldo(target.getSaldo().add(dto.getImporto()));

            contoRepository.save(sorgente);
            contoRepository.save(target);

            tx.setDirezione(DirezioneTransazione.USCITA.name());

            logger.info("Bonifico completato con successo da {} a {}. Importo: {} {}",
                    sorgente.getNumeroConto(),
                    target.getNumeroConto(),
                    dto.getImporto(),
                    dto.getValuta());

            // Registra transazione lato destinatario
            Transazione inEntrata = new Transazione();
            inEntrata.setConto(target);
            inEntrata.setTipoTransazione(dto.getTipoTransazione());
            inEntrata.setImporto(dto.getImporto());
            inEntrata.setValuta(dto.getValuta());
            inEntrata.setDirezione(DirezioneTransazione.ENTRATA.name());
            inEntrata.setDescrizione("Bonifico ricevuto da " + sorgente.getUser().getNome() + " " + sorgente.getUser().getCognome() + " per " + dto.getDescrizione());
        
            logger.debug("Registrazione transazione in entrata per il destinatario {}", target.getNumeroConto());
            transazioneRepository.save(inEntrata);
        } else {
            logger.warn("Tipo di transazione sconosciuto: {}", dto.getTipoTransazione());
        }

        transazioneRepository.save(tx);
        logger.info("Transazione completata e salvata nel sistema: {}", tx);

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
