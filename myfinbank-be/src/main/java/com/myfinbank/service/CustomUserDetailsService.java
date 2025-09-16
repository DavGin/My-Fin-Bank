package com.myfinbank.service;

import com.myfinbank.entity.User;
import com.myfinbank.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository repo;

    public CustomUserDetailsService(UserRepository repo) { this.repo = repo; }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Recupera l'utente dal repository
        User user = repo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utente con " + username + " non trovato"));

        // Verifica che il ruolo esista
        if (user.getRuolo() == null) {
            throw new IllegalStateException("L'utente non ha ruoli associati.");
        }

        // Crea una lista di Authorities
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(user.getRuolo().getName())
        );

        // Costruisce l'oggetto UserDetails
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities) // Ruoli
                .disabled(!user.getEnabled()) // Controlla se è disabilitato
                .build();
    }



}
