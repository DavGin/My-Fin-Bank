package com.myfinbank.dto.mutuo;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter
@Setter
public class MutuoRequestCreateDto {

    @NotNull
    @Min(value = 1)
    private BigDecimal importo;

    @NotNull
    @Min(value = 1)
    private int durataMesi;

    @NotNull
    @Min(value = 0)
    private BigDecimal tassoInteresse;

    private String motivoMutuo;



}
