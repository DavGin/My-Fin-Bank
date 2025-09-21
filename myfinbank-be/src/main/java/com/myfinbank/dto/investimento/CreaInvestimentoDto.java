package com.myfinbank.dto.investimento;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreaInvestimentoDto {

    @NotNull
    private String tipoInvestimento;

    @NotNull
    @Min(1)
    private BigDecimal importoInvestito;

    @NotNull
    private BigDecimal tassoRitornoPrevisto;

    @NotNull
    private int durataMesi;
}
