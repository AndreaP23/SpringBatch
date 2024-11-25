package org.si2001.batch_rentalcar.config;

import lombok.extern.slf4j.Slf4j;
import org.example.entities.Prenotazione;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class PrenotazioneRepositoryProcess {

    @Bean
    public ItemProcessor<Prenotazione, Prenotazione> repositoryItemProcessor() {
        return prenotazione -> {
            log.info("Elaborazione della prenotazione con ID: {}", prenotazione.getPrenotazioneId());

            // Logica di trasformazione o arricchimento
            // Esempio: Aggiungi una nota se la data di inizio è già passata
            if (prenotazione.getDataInizio().isBefore(java.time.LocalDate.now())) {
                prenotazione.setNote("Elaborato - Data di inizio passata");
            }

            log.info("Prenotazione processata: {}", prenotazione);
            return prenotazione;
        };
    }
}
