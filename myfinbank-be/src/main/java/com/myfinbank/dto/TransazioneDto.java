package com.myfinbank.dto;

import com.myfinbank.entity.Transazione;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class TransazioneDto {
    private Long id;
    private String tipoTransazione;
    private BigDecimal importo;
    private String valuta;
    private LocalDateTime dataTransazione;
    private String descrizione;
    private String numeroConto;
    private String targetIban;

    public static TransazioneDto fromEntity(Transazione tx) {
        TransazioneDto dto = new TransazioneDto();
        dto.setId(tx.getId());
        dto.setTipoTransazione(tx.getTipoTransazione());
        dto.setImporto(tx.getImporto());
        dto.setValuta(tx.getValuta());
        dto.setDataTransazione(tx.getDataTransazione());
        dto.setDescrizione(tx.getDescrizione());
        dto.setNumeroConto(tx.getConto().getNumeroConto());
        return dto;
    }
}
