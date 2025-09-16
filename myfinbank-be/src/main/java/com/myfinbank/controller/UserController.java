package com.myfinbank.controller;

import com.myfinbank.dto.PasswordUpdateRequest;
import com.myfinbank.dto.ProfileUpdateRequest;
import com.myfinbank.dto.UserProfileDto;
import com.myfinbank.entity.User;
import com.myfinbank.repository.UserRepository;
import com.myfinbank.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/profile")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    private User getCurrentUser(String username) {
        User user = userService.getProfile(username);
        if (user == null) {
            throw new RuntimeException("Utente non trovato");
        }
        return user;
    }

    // Accessibile a tutti gli utenti autenticati
    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public UserProfileDto getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return UserProfileDto.fromEntity(userService.getProfile(getCurrentUser(String.valueOf(userDetails)).getUsername()));
    }

    // Accessibile SOLO agli admin
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserProfileDto> getAllUsers() {
        return userService.findAll();
    }


    // ✏️ Aggiorna nome/cognome
    @PutMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public UserProfileDto updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ProfileUpdateRequest request) {
        return UserProfileDto.fromEntity(userService.updateProfile(getCurrentUser(String.valueOf(userDetails)).getUsername(), request));
    }

    // 🔑 Aggiorna password
    @PutMapping("/password")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public String updatePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody PasswordUpdateRequest request) {
        userService.updatePassword(getCurrentUser(String.valueOf(userDetails)).getUsername(), request);
        return "Password aggiornata con successo";
    }

}
