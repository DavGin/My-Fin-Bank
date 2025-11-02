package com.myfinbank.dto;

import com.myfinbank.entity.Conto;
import com.myfinbank.entity.User;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CartaRequest {

    private String numeroConto;
    private String tipo;

}
