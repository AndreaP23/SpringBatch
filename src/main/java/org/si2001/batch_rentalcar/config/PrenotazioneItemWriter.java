package org.si2001.batch_rentalcar.config;

import org.example.dao.PrenotazioneRepository;
import org.example.entities.Prenotazione;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
public class PrenotazioneItemWriter implements ItemWriter<Prenotazione> {
    private final PrenotazioneRepository prenotazioneRepository;

    public PrenotazioneItemWriter(PrenotazioneRepository prenotazioneRepository) {
        this.prenotazioneRepository = prenotazioneRepository;
    }

    @Override
    public void write(Chunk<? extends Prenotazione> chunk) throws Exception{
        prenotazioneRepository.saveAll(chunk.getItems());
    }
}
