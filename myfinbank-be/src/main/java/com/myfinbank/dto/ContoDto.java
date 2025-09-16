package com.myfinbank.dto;

import com.myfinbank.entity.Conto;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ContoDto {

    private Long id;
    private String nome;
    private String type;
    private String currency;
    private BigDecimal balance;

    public static ContoDto fromEntity(Conto acc) {
        ContoDto dto = new ContoDto();
        dto.setId(acc.getId());
        dto.setNome(acc.getNome());
        dto.setType(acc.getType());
        dto.setCurrency(acc.getCurrency());
        dto.setBalance(acc.getBalance());
        return dto;
    }
}
