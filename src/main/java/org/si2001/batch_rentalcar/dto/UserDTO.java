package org.si2001.batch_rentalcar.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class UserDTO {
    private Long userId;
    private String nome;
    private String cognome;
    private String email;
    private String telefono;
    private String password;
    private LocalDate dataNascita;
    private Integer ruolo;
    private List<Long> prenotazioniIds;
}
