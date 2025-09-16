package com.myfinbank.service;

import com.myfinbank.dto.ContoDto;
import com.myfinbank.dto.UserProfileDto;
import com.myfinbank.entity.User;
import com.myfinbank.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) { this.userRepository = userRepository; }

    @Transactional(readOnly = true)
    public User getProfile(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return user;
    }

    public List<UserProfileDto> findAll() {
        return userRepository.findAll().stream().map(UserProfileDto::fromEntity).collect(Collectors.toList());

    }
}
