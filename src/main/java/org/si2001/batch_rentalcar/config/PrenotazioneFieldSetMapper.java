package org.si2001.batch_rentalcar.config;

import org.si2001.batch_rentalcar.dto.PrenotazioneDTO;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PrenotazioneFieldSetMapper implements FieldSetMapper<PrenotazioneDTO> {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public PrenotazioneDTO mapFieldSet(FieldSet fieldSet) throws BindException {
        return PrenotazioneDTO.builder()
                .userId(fieldSet.readLong("userId"))
                .veicoloId(fieldSet.readLong("veicoloId"))
                .dataPrenotazione(LocalDate.parse(fieldSet.readString("dataPrenotazione"), DATE_FORMATTER))
                .dataInizio(LocalDate.parse(fieldSet.readString("dataInizio"), DATE_FORMATTER))
                .dataFine(LocalDate.parse(fieldSet.readString("dataFine"), DATE_FORMATTER))
                .note(fieldSet.readString("note"))
                .build();
    }

}
