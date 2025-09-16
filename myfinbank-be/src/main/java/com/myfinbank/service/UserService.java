package com.myfinbank.service;

import com.myfinbank.dto.ContoDto;
import com.myfinbank.dto.PasswordUpdateRequest;
import com.myfinbank.dto.ProfileUpdateRequest;
import com.myfinbank.dto.UserProfileDto;
import com.myfinbank.entity.User;
import com.myfinbank.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) { this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public User getProfile(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return user;
    }

    public List<UserProfileDto> findAll() {
        return userRepository.findAll().stream().map(UserProfileDto::fromEntity).collect(Collectors.toList());

    }

    @Transactional
    public User updateProfile(String username, ProfileUpdateRequest dto) {
        User user = userRepository.findByUsername(username).orElseThrow();
        user.setNome(dto.getNome());
        user.setCognome(dto.getCognome());
        user.setCodiceFiscale(dto.getCodiceFiscale());
        user.setDataNascita(dto.getDataNascita());
        userRepository.save(user);

        return getProfile(username);
    }

    public void updatePassword(String username, PasswordUpdateRequest request) {
        User user = userRepository.findByUsername(username).orElseThrow();
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

}
