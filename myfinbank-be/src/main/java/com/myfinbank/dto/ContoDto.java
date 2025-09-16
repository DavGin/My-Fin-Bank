package com.myfinbank.dto;

import com.myfinbank.entity.Conto;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ContoDto {

    private Long id;
    private String numeroConto;
    private String tipo;
    private String iban;
    private String valuta;
    private BigDecimal saldo;

    public static ContoDto fromEntity(Conto conto) {
        ContoDto dto = new ContoDto();
        dto.setId(conto.getId());
        dto.setNumeroConto(conto.getNumeroConto());
        dto.setTipo(conto.getTipo());
        dto.setIban(conto.getIban());
        dto.setValuta(conto.getValuta());
        dto.setSaldo(conto.getSaldo());
        return dto;
    }
}
