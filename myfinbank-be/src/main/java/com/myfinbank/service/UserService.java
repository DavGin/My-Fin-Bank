package com.myfinbank.service;

import com.myfinbank.dto.UserProfileDto;
import com.myfinbank.entity.User;
import com.myfinbank.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) { this.userRepository = userRepository; }

    @Transactional(readOnly = true)
    public User getProfile(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return user;
    }
}
