package com.myfinbank.dto;

import com.myfinbank.entity.User;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UserProfileDto {

    private Long id;
    private String username;
    private String email;
    private String nome;
    private String cognome;
    private String password;
    private String codiceFiscale;
    private LocalDate dataNascita;
    private String role;

    public static UserProfileDto fromEntity(User user) {
        UserProfileDto dto = new UserProfileDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setNome(user.getNome());
        dto.setCognome(user.getCognome());
        dto.setRole(user.getPassword());
        dto.setCodiceFiscale(user.getCodiceFiscale());
        dto.setDataNascita(user.getDataNascita());
        return dto;
    }

}
