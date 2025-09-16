package com.myfinbank.repository;

import com.myfinbank.entity.Conto;
import com.myfinbank.entity.Transazione;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransazioneRepository extends JpaRepository<Transazione, Long> {
    List<Transazione> findByConto(Conto conto);
}
