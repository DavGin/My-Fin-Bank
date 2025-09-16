package com.myfinbank.entity;

import aj.org.objectweb.asm.commons.Remapper;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "USERS")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Long id;

    @Size(max = 255)
    @NotNull
    @Column(name = "EMAIL", nullable = false)
    private String email;

    @Size(max = 255)
    @NotNull
    @Column(name = "PASSWORD", nullable = false)
    private String password;

    @Size(max = 100)
    @Column(name = "NOME", length = 100)
    private String nome;

    @Size(max = 100)
    @Column(name = "COGNOME", length = 100)
    private String cognome;

    @Size(max = 100)
    @Column(name = "CODICE_FISCALE", length = 100)
    private String codiceFiscale;

    @Column(name = "DATA_NASCITA")
    private LocalDate dataNascita;

    @Size(max = 50)
    @ColumnDefault("'USER'")
    @Column(name = "RUOLO", length = 50)
    private String ruolo;

    @Column(name = "ENABLED")
    private Boolean enabled = true;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Size(max = 100)
    @NotNull
    @Column(name = "USERNAME", nullable = false, length = 100)
    private String username;

}
