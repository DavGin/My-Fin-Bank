package com.myfinbank.dto;

import com.myfinbank.entity.Carta;
import com.myfinbank.entity.Conto;
import com.myfinbank.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class CartaDto {

    private Long id;
    private String numeroCarta;
    private String numeroConto;
    private String cvc;
    private String dataScadenza;
    private String circuito;
    private String tipo;
    private BigDecimal limiteGiornaliero;
    private BigDecimal limiteMensile;
    private LocalDateTime dataCreazione = LocalDateTime.now();
    private BigDecimal saldoCarta;
    private BigDecimal plafond;
    private String stato;
    private LocalDateTime ultimoUtilizzo;
    private String pin;

    public static CartaDto fromEntity(Carta carta) {
        CartaDto cartaDto = new CartaDto();
        cartaDto.setId(carta.getId());
        cartaDto.setNumeroCarta(carta.getNumeroCarta());
        // ✅ Evita NPE
        if (carta.getConto() != null) {
            cartaDto.setNumeroConto(carta.getConto().getNumeroConto());
        } else {
            cartaDto.setNumeroConto(null);
        }
        cartaDto.setCvc(carta.getCvv());
        cartaDto.setDataScadenza(String.valueOf(carta.getDataScadenza()));
        cartaDto.setCircuito(carta.getCircuito());
        cartaDto.setTipo(carta.getTipo());
        cartaDto.setLimiteGiornaliero(carta.getLimiteGiornaliero());
        cartaDto.setLimiteMensile(carta.getLimiteMensile());
        cartaDto.setDataCreazione(carta.getDataCreazione());
        cartaDto.setSaldoCarta(carta.getSaldoCarta());
        cartaDto.setPlafond(carta.getPlafond());
        cartaDto.setStato(carta.getStato());
        cartaDto.setUltimoUtilizzo(carta.getUltimoUtilizzo());
        cartaDto.setPin(carta.getPin());
        return cartaDto;
    }

    public static List<CartaDto> fromEntity(List<Carta> carta) {
        return carta.stream()
                .map(CartaDto::fromEntity)
                .collect(Collectors.toList());
    }


}
