package com.myfinbank.dto.mutuo;

import com.myfinbank.entity.Mutuo;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class MutuoRequestDto {

    private Long id;
    private String numeroPratica;
    private BigDecimal importo;
    private int durataMesi;
    private BigDecimal tassoInteresse;
    private LocalDateTime dataCreazione;
    private String motivoMutuo;
    private String stato;
    private String motivoRifiuto;

    public static MutuoRequestDto fromEntity(Mutuo mutuo) {
        MutuoRequestDto dto = new MutuoRequestDto();
        dto.setId(mutuo.getId());
        dto.setNumeroPratica(mutuo.getNumeroPratica());
        dto.setImporto(mutuo.getImporto());
        dto.setDurataMesi(mutuo.getDurataMesi());
        dto.setTassoInteresse(mutuo.getTassoInteresse());
        dto.setDataCreazione(mutuo.getDataCreazione());
        dto.setMotivoMutuo(mutuo.getMotivoMutuo());
        dto.setStato(mutuo.getStato());
        dto.setMotivoRifiuto(mutuo.getMotivoRifiuto());
        return dto;
    }

}
