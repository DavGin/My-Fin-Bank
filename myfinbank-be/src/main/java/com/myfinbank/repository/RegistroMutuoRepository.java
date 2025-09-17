package com.myfinbank.repository;

import com.myfinbank.entity.Mutuo;
import com.myfinbank.entity.RegistroMutuo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegistroMutuoRepository extends JpaRepository<RegistroMutuo, Long> {
    List<RegistroMutuo> findByMutuo(Mutuo mutuo);
}
