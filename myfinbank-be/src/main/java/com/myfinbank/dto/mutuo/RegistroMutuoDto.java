package com.myfinbank.dto.mutuo;

import com.myfinbank.entity.Mutuo;
import com.myfinbank.entity.RegistroMutuo;
import com.myfinbank.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RegistroMutuoDto {

    private Long id;
    private String adminEmail;
    private String stato;
    private LocalDateTime dataRegistrazione;
    private String motivo;

    public static RegistroMutuoDto fromEntity(RegistroMutuo registroMutuo) {
        RegistroMutuoDto dto = new RegistroMutuoDto();
        dto.setId(registroMutuo.getId());
        dto.setAdminEmail(registroMutuo.getMutuo().getUser().getEmail());
        dto.setStato(registroMutuo.getStato());
        dto.setDataRegistrazione(registroMutuo.getDataRegistrazione());
        dto.setMotivo(registroMutuo.getMotivo());
        return dto;
    }
}
