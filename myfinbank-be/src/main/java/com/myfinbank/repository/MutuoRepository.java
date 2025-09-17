package com.myfinbank.repository;

import com.myfinbank.entity.Mutuo;
import com.myfinbank.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MutuoRepository extends JpaRepository<Mutuo, Long> {

    List<Mutuo> findByUser(User user);

    Mutuo findByNumeroPratica(String numeroPratica);
}
