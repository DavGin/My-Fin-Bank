package com.myfinbank.dto.mutuo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class RateDto {

    private int numeroRata;
    private LocalDateTime scadenza;
    private BigDecimal quotaCapitale;
    private BigDecimal interessi;
    private BigDecimal rataTotale;
    private BigDecimal saldoRimanente;
    private String statoRata;



}
