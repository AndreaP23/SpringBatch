package org.si2001.batch_rentalcar.mapper;


import org.example.entities.Prenotazione;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.si2001.batch_rentalcar.dto.PrenotazioneDTO;

@Mapper(componentModel = "spring")
public interface PrenotazioneMapper {
    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "veicolo.veicoloId", target = "veicoloId")
    PrenotazioneDTO toDTO(Prenotazione prenotazione);

    @Mapping(source = "userId", target = "user.userId")
    @Mapping(source = "veicoloId", target = "veicolo.veicoloId")
    Prenotazione toEntity(PrenotazioneDTO prenotazioneDTO);
}
