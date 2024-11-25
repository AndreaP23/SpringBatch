package org.si2001.batch_rentalcar.config;

import lombok.extern.slf4j.Slf4j;
import org.example.dao.PrenotazioneRepository;
import org.example.entities.Prenotazione;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class PrenotazioneRepositoryWriter {

    @Bean
    public ItemWriter<Prenotazione> repositoryItemWriter(PrenotazioneRepository prenotazioneRepository) {
        return items -> {
            log.info("Scrittura di {} prenotazioni nel database.", items.size());
            prenotazioneRepository.saveAll(items);
            log.info("Scrittura completata.");
        };
    }
}
