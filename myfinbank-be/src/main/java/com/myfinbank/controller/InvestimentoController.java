package com.myfinbank.controller;

import com.myfinbank.dto.investimento.CreaInvestimentoDto;
import com.myfinbank.dto.investimento.InvestimentoDto;
import com.myfinbank.dto.investimento.ProiezioneInvestimentoDto;
import com.myfinbank.dto.investimento.SimulazioneInvestimentoDto;
import com.myfinbank.service.InvestimentoService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/investimento")
public class InvestimentoController {

    private final InvestimentoService investimentoService;

    public InvestimentoController(InvestimentoService investimentoService) {
        this.investimentoService = investimentoService;
    }

    @PostMapping("/createInvestimento")
    @PreAuthorize("hasRole('USER')")
    public InvestimentoDto createInvestimento(@Valid @RequestBody CreaInvestimentoDto dto) {
        return investimentoService.createInvestment(dto);
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public List<InvestimentoDto> getUserInvestments() {
        return investimentoService.getListaInvestimentiAttivi();
    }

    @PostMapping("/chiudiInvestimento/{identificativo}")
    @PreAuthorize("hasRole('USER')")
    public InvestimentoDto chiudiInvestimento(@PathVariable String identificativo) {
        return investimentoService.chiudiInvestimento(identificativo);
    }

    @GetMapping("/proiezioneInvestimento/{identificativo}/{anni}")
    @PreAuthorize("hasRole('USER')")
    public ProiezioneInvestimentoDto proiezioneInvestimento(
            @PathVariable String identificativo ,
            @RequestParam(defaultValue = "5") int anni
    ) {
        return investimentoService.proiezioneInvestimento(identificativo, anni);
    }

    @PostMapping("/simulaInvestimento")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ProiezioneInvestimentoDto simulaInvestimento(@Valid @RequestBody SimulazioneInvestimentoDto request) {
        return investimentoService.simulaInvestimento(request);
    }
}
