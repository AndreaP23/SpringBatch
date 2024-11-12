package org.si2001.batch_rentalcar.config;

import lombok.extern.slf4j.Slf4j;
import org.example.dao.PrenotazioneRepository;
import org.example.entities.Prenotazione;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class RepositoryItemReaderConfig {

    /**
     * Configura un RepositoryItemReader per leggere le prenotazioni dal database.
     */
    @Bean
    @Transactional(readOnly = true) // Garantisce la lettura dei dati senza modifica
    public RepositoryItemReader<Prenotazione> repositoryItemReader(PrenotazioneRepository prenotazioneRepository) {
        RepositoryItemReader<Prenotazione> reader = new RepositoryItemReader<>();
        reader.setRepository(prenotazioneRepository);
        reader.setMethodName("findAllByToday"); // Metodo personalizzato nella repository
        reader.setPageSize(10);

        // Configura l'ordinamento
        Map<String, Sort.Direction> sortMap = new HashMap<>();
        sortMap.put("prenotazioneId", Sort.Direction.ASC); // Ordine crescente per prenotazioneId
        reader.setSort(sortMap);

        log.info("RepositoryItemReader configurato correttamente.");
        return reader;
    }
}
