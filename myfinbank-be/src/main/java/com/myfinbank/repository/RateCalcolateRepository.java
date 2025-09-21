package com.myfinbank.repository;

import com.myfinbank.entity.Mutuo;
import com.myfinbank.entity.RateCalcolate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RateCalcolateRepository extends JpaRepository<RateCalcolate, Long> {

    // Cerca rate per l'oggetto Mutuo
    List<RateCalcolate> findAllByMutuo(Mutuo mutuo);

    // Verifica esistenza di una rata per Mutuo e Numero Rata
    boolean existsByMutuoAndNumeroRataAndStatoRata(Mutuo mutuo, int numeroRata, String statoRata);

    // Cerca rata specifica per Mutuo e Numero Rata
    RateCalcolate findByMutuoAndNumeroRata(Mutuo mutuo, int numeroRata);

    // Cerca rate per stato specifico
    List<RateCalcolate> findByStatoRata(String statoRata);
}
