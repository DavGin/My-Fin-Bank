package com.myfinbank.repository;

import com.myfinbank.entity.Conto;
import com.myfinbank.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface ContoRepository extends JpaRepository<Conto, Long> {

    Collection<Conto> findByUser(User user);
}
