package org.si2001.batch_rentalcar.mapper;

import org.example.entities.Veicolo;
import org.mapstruct.Mapper;
import org.si2001.batch_rentalcar.dto.VeicoloDTO;

@Mapper(componentModel = "spring")
public interface VeicoloMapper {

    VeicoloDTO toDTO(Veicolo veicolo);

    Veicolo toEntity(VeicoloDTO veicoloDTO);
}
