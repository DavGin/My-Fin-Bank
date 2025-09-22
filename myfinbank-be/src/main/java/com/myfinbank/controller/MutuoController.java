package com.myfinbank.controller;


import com.myfinbank.dto.mutuo.*;
import com.myfinbank.service.MutuoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/mutui")
public class MutuoController {

    private final MutuoService mutuoService;

    public MutuoController(MutuoService mutuoService) {
        this.mutuoService = mutuoService;
    }

    @PostMapping("/createMutuo")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public MutuoRequestDto createMutuo(@Valid @RequestBody MutuoRequestCreateDto dto) {
        MutuoRequestDto mutuoRequestDto = mutuoService.createMutuoRequest(dto);
        mutuoService.calcolaRata(mutuoRequestDto.getNumeroPratica());
        return mutuoRequestDto;
    }

    @GetMapping("/listMutuiUtente")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public List<MutuoRequestDto> listMutuiUtente() {
        return mutuoService.listMutuiUtente();
    }

    @GetMapping("/getMutuo/{numeroPratica}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public MutuoRequestDto getMutuo(@PathVariable String numeroPratica) {
        return mutuoService.getMutuo(numeroPratica);
    }

    // APPROVA richiesta mutuo
    @PostMapping("/approvaMutuo")
    @PreAuthorize("hasRole('ADMIN')")
    public MutuoRequestDto approvaMutuo(@Valid @RequestBody EsitoMutuoDto dto) {
        return mutuoService.changeStatoMutuo(dto);
    }

    // RIFIUTA richiesta mutuo
    @PostMapping("/rejectMutuo")
    @PreAuthorize("hasRole('ADMIN')")
    public MutuoRequestDto rejectMutuo(@Valid @RequestBody EsitoMutuoDto dto) {
        return mutuoService.changeStatoMutuo(dto);
    }

    @GetMapping("/getStoricoRegistroMutuo/{numeroPratica}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<MutuoRequestDto> getStoricoRegistroMutuo(@PathVariable String numeroPratica) {
        return mutuoService.getStoricoRegistroMutuo(numeroPratica);
    }

    @GetMapping("/getListaStoricoRegistroMutuo")
    @PreAuthorize("hasRole('ADMIN')")
    public List<MutuoRequestDto> getListaStoricoRegistroMutuo() {
        List<MutuoRequestDto> listaStoricoRegistroMutuo = mutuoService.getListaStoricoRegistroMutuo();

        return listaStoricoRegistroMutuo;
    }

//    @GetMapping("/calcolaRata/{numeroPratica}")
//    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
//    public List<RateDto> calcolaRata(@PathVariable String numeroPratica) {
//        return mutuoService.calcolaRata(numeroPratica);
//    }

    @PostMapping("/simulatoreMutuo")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public List<RateDto> simulatoreMutuo(@Valid @RequestBody SimulazioneMutuoDto request) {
        return mutuoService.simulatoreMutuo(request);
    }

    @PostMapping("/pagaRata/{numeroPratica}/{numeroRata}/{numeroConto}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> pagaRata(
            @PathVariable String numeroPratica,
            @PathVariable int numeroRata,
            @PathVariable String numeroConto) {
        mutuoService.pagaRata(numeroPratica,numeroRata, numeroConto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/findByNumeroPratica/{numeroPratica}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public List<RateDto> findByNumeroPratica(@PathVariable String numeroPratica) {
        return mutuoService.findAllRatesByNumeroPraticaDto(numeroPratica);
    }

}
