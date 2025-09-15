package com.myfinbank.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "ACCOUNTS")
public class Account {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID", nullable = false)
  private Long id;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "USER_ID", nullable = false)
  private User user;

  @Size(max = 150)
  @Column(name = "NOME", length = 150)
  private String nome;

  @Size(max = 50)
  @Column(name = "\"TYPE\"", length = 50)
  private String type;

  @Size(max = 10)
  @Column(name = "CURRENCY", length = 10)
  private String currency;

  @ColumnDefault("0")
  @Column(name = "BALANCE", precision = 19, scale = 2)
  private BigDecimal balance;

  @ColumnDefault("CURRENT_TIMESTAMP")
  @Column(name = "CREATED_AT")
  private LocalDateTime createdAt;

}
