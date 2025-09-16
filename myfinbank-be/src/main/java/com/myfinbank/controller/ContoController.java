package com.myfinbank.controller;

import com.myfinbank.dto.ContoDto;
import com.myfinbank.entity.User;
import com.myfinbank.service.ContoService;
import com.myfinbank.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/conti")
public class ContoController {

    private final ContoService service;
    private final UserService userService;

    public ContoController(ContoService service , UserService userService) {
        this.service = service;
        this.userService = userService;
    }

    @GetMapping
    public List<ContoDto> list(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getProfile(userDetails.getUsername());
        return service.listConti(user.getUsername());
    }

    @PostMapping
    public ResponseEntity<?> create(@AuthenticationPrincipal UserDetails userDetails,
                                    @Valid @RequestBody ContoDto dto) {
        service.createConto(userDetails.getUsername(), dto);
        return ResponseEntity.status(201).build();
    }
}
