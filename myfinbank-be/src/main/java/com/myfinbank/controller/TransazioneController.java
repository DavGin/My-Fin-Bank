package com.myfinbank.controller;

import com.myfinbank.dto.TransazioneDto;
import com.myfinbank.service.TransazioneService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transazioni")
public class TransazioneController {

    private final TransazioneService transazioneService;

    public TransazioneController(TransazioneService transazioneService) {
        this.transazioneService = transazioneService;
    }

    @PostMapping("/creaTransazione/{numeroConto}")
    public ResponseEntity<TransazioneDto> creaTransazione(
            @PathVariable String numeroConto,
            @Valid @RequestBody TransazioneDto transazioneDto) {
        TransazioneDto nuovaTransazione = transazioneService.creaTransazione(numeroConto, transazioneDto);
        return ResponseEntity.ok(nuovaTransazione);
    }

    @GetMapping("/listTransazioni/{numeroConto}")
    public ResponseEntity<List<TransazioneDto>> listTransazioni(@PathVariable String numeroConto) {
        List<TransazioneDto> transazioni = transazioneService.listTransazioni(numeroConto);
        return ResponseEntity.ok(transazioni);
    }
}
