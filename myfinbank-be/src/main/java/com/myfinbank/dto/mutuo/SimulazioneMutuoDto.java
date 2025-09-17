package com.myfinbank.dto.mutuo;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SimulazioneMutuoDto {

    @NotNull
    @Min(1)
    private BigDecimal importo;

    @NotNull
    @Min(1)
    private Integer durateMesi;

    @NotNull
    @Min(0)
    private BigDecimal tassoInteresse;

    private String motivo;

}
