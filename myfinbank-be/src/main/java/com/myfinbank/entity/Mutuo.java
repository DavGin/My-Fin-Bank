package com.myfinbank.entity;


import com.myfinbank.utils.StatoMutuo;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "MUTUI")
public class Mutuo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @Column(name = "NUMERO_PRATICA", nullable = false, unique = true, length = 10)
    private String numeroPratica;

    @Column(name = "IMPORTO", nullable = false)
    private BigDecimal importo;

    @Column(name = "DURATA_MESI", nullable = false)
    private int durataMesi;

    @Column(name = "TASSO_INTERESSE", nullable = false)
    private BigDecimal tassoInteresse;

    @Column(name = "DATA_CREAZIONE", nullable = false)
    private LocalDateTime dataCreazione = LocalDateTime.now();

    @Column(name = "MOTIVO_MUTUO")
    private String motivoMutuo;

    @Column(name = "STATO", nullable = false)
    private String stato = String.valueOf(StatoMutuo.PENDING);

    @Column(name = "MOTIVO_RIFIUTO")
    private String motivoRifiuto;


}
