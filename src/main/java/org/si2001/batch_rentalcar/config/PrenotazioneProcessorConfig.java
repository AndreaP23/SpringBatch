package org.si2001.batch_rentalcar.config;

import org.example.dao.PrenotazioneRepository;
import org.example.dao.UserRepository;
import org.example.dao.VeicoloRepository;
import org.example.entities.Prenotazione;
import org.si2001.batch_rentalcar.dto.PrenotazioneDTO;
import org.si2001.batch_rentalcar.mapper.PrenotazioneMapper;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PrenotazioneProcessorConfig {

    @Bean
    public ItemProcessor<PrenotazioneDTO, Prenotazione> importPrenotazioneProcessor(PrenotazioneMapper prenotazioneMapper,
                                                                                    UserRepository userRepository,
                                                                                    VeicoloRepository veicoloRepository,
                                                                                    PrenotazioneRepository prenotazioneRepository) {
        return new PrenotazioneItemProcessor(prenotazioneMapper, userRepository, veicoloRepository, prenotazioneRepository);
    }
}
