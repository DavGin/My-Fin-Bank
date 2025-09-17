package com.myfinbank.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "REGISTRO_MUTUI")
public class RegistroMutuo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MUTUO_ID", nullable = false)
    private Mutuo mutuo;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user; 

    @NotNull
    @Column(name = "DATA_REGISTRAZIONE", nullable = false)
    private LocalDateTime dataRegistrazione;

    @NotNull
    @Column(name = "STATO", nullable = false)
    private String stato;

    @Column(name = "MOTIVO", nullable = false)
    private String motivo;


}
