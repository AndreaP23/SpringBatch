package org.si2001.batch_rentalcar.config;

import lombok.extern.slf4j.Slf4j;
import org.si2001.batch_rentalcar.dto.PrenotazioneDTO;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

@Slf4j
@Configuration
public class PrenotazioneItemReader {

    @Bean(name = "flatFileItemReader")
    @StepScope
    public FlatFileItemReader<PrenotazioneDTO> flatFileItemReader(@Value("#{jobParameters['inputFile']}") String prenotazioniFile) {
        FlatFileItemReader<PrenotazioneDTO> reader = new FlatFileItemReader<>();
        log.info("Inizializzazione del Reader per il file: {}", prenotazioniFile);
        reader.setName("PRENOTAZIONI_READER");
        reader.setLinesToSkip(1); // Salta l'intestazione
        reader.setLineMapper(lineMapper());
        reader.setStrict(false);
        reader.setResource(new FileSystemResource(prenotazioniFile));
        return reader;
    }

    private DefaultLineMapper<PrenotazioneDTO> lineMapper() {
        DefaultLineMapper<PrenotazioneDTO> lineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(","); // Delimitatore CSV
        tokenizer.setNames("userId", "veicoloId", "dataPrenotazione", "dataInizio", "dataFine", "note"); // Campi attesi
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(new PrenotazioneFieldSetMapper());
        return lineMapper;
    }
}
