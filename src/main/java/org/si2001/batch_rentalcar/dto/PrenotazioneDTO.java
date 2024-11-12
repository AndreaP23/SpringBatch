package org.si2001.batch_rentalcar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrenotazioneDTO {

    private Long userId;
    private Long veicoloId;
    private LocalDate dataPrenotazione;
    private LocalDate dataInizio;
    private LocalDate dataFine;
    private String note;

}
