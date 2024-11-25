package org.si2001.batch_rentalcar.config;

import lombok.extern.slf4j.Slf4j;
import org.example.dao.PrenotazioneRepository;
import org.example.entities.Prenotazione;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.domain.Sort;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class PrenotazioneRepositoryReader {

    @Bean(name = "repositoryPrenotazioniItemReader")
    @Scope(value = "step", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public RepositoryItemReader<Prenotazione> repositoryItemReader(PrenotazioneRepository prenotazioneRepository) {
        RepositoryItemReader<Prenotazione> reader = new RepositoryItemReader<>();
        reader.setRepository(prenotazioneRepository);
        reader.setMethodName("findAllByToday");
        reader.setPageSize(10);

        // Specifica l'ordinamento esplicito per il Reader
        Map<String, Sort.Direction> sortMap = new HashMap<>();
        sortMap.put("prenotazioneId", Sort.Direction.ASC); // Ordina per prenotazioneId in ordine crescente
        reader.setSort(sortMap);


        log.info("RepositoryItemReader configurato per leggere le prenotazioni di oggi dal database con ordinamento.");
        return reader;
    }
}
