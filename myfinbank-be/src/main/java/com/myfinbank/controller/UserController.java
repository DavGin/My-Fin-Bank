package com.myfinbank.controller;

import com.myfinbank.dto.UserProfileDto;
import com.myfinbank.entity.User;
import com.myfinbank.repository.UserRepository;
import com.myfinbank.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Accessibile a tutti gli utenti autenticati
    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public UserProfileDto getProfile(Authentication authentication) {
        User user = userService.getProfile(authentication.getName());
        return UserProfileDto.fromEntity(user);
    }

    // Accessibile SOLO agli admin
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserProfileDto> getAllUsers() {
        return userService.findAll();
    }
}
