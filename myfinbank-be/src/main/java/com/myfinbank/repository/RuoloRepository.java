package com.myfinbank.repository;

import com.myfinbank.entity.Ruolo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RuoloRepository extends JpaRepository<Ruolo, Long> {
    Optional<Object> findByNome(String ruolo);
}
