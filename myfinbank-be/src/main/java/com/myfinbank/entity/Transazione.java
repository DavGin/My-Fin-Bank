package com.myfinbank.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "TRANSAZIONI")
public class Transazione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CONTO_ID", nullable = false)
    private Conto conto;

    @Column(name = "TIPO_TRANSAZIONE", nullable = false)
    private String tipoTransazione;

    @Column(name = "IMPORTO", nullable = false)
    private BigDecimal importo;

    @Column(name = "VALUTA", nullable = false)
    private String valuta;

    @Column(name = "DATA_TRANSAZIONE", nullable = false)
    private LocalDateTime dataTransazione = LocalDateTime.now();

    @Column(name = "DESCRIZIONE", nullable = false)
    private String descrizione;
}
