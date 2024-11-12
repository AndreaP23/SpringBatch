package org.si2001.batch_rentalcar.config;


import lombok.extern.slf4j.Slf4j;
import org.example.dao.PrenotazioneRepository;
import org.example.dao.UserRepository;
import org.example.dao.VeicoloRepository;
import org.example.entities.Prenotazione;
import org.si2001.batch_rentalcar.dto.PrenotazioneDTO;
import org.si2001.batch_rentalcar.mapper.PrenotazioneMapper;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
public class PrenotazioneItemProcessor implements ItemProcessor<PrenotazioneDTO, Prenotazione> {

    private final PrenotazioneMapper prenotazioneMapper;
    private final UserRepository userRepository;
    private final VeicoloRepository veicoloRepository;
    private final PrenotazioneRepository prenotazioneRepository;

    public PrenotazioneItemProcessor(PrenotazioneMapper prenotazioneMapper, UserRepository userRepository, VeicoloRepository veicoloRepository, PrenotazioneRepository prenotazioneRepository) {
        this.prenotazioneMapper = prenotazioneMapper;
        this.userRepository = userRepository;
        this.veicoloRepository = veicoloRepository;
        this.prenotazioneRepository = prenotazioneRepository;
    }

    @Override
    public Prenotazione process(PrenotazioneDTO prenotazioneDTO) throws Exception {
        // Controllo se l'utente esiste
        if (!userRepository.existsById(prenotazioneDTO.getUserId())) {
            log.warn("Utente con ID {} non esiste. Prenotazione ignorata.", prenotazioneDTO.getUserId());
            return null;
        }

        // Controllo se esiste il veicolo
        if(!veicoloRepository.existsById(prenotazioneDTO.getVeicoloId())) {
            log.warn("Il Veicolo con ID {} non esiste. Prenotazione ignorata.", prenotazioneDTO.getVeicoloId());
            return null;
        }


        if(prenotazioneRepository.existsByVeicolo_VeicoloId(prenotazioneDTO.getVeicoloId())) {
            log.warn("Il Veicolo con ID {} non è disponibile (già prenotato). Prenotazione ignorata.", prenotazioneDTO.getVeicoloId());
            return null;
        }

        // Controllo delle date
        LocalDate oggi = LocalDate.now();
        LocalDate dataInizio = prenotazioneDTO.getDataInizio();
        LocalDate dataFine = prenotazioneDTO.getDataFine();

        if (dataInizio.isBefore(oggi)) {
            log.warn("La data di inizio deve essere oggi o successiva. Prenotazione ignorata.");
            return null;
        }

        if (dataFine.isBefore(dataInizio.plusDays(1))) {
            log.warn("La data di fine deve essere almeno un giorno dopo la data di inizio. Prenotazione ignorata.");
            return null;
        }

        // Mappa il DTO all'entità Prenotazione
        return prenotazioneMapper.toEntity(prenotazioneDTO);



    }
}
