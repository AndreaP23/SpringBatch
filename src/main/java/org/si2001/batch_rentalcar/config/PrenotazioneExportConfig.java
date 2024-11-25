package org.si2001.batch_rentalcar.config;

import lombok.extern.slf4j.Slf4j;
import org.example.entities.Prenotazione;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.io.IOException;

@Slf4j
@Configuration
public class PrenotazioneExportConfig {

    @Value("${prenotazione.export.path}")
    private String exportFilePath;

    @Bean(name = "prenotazioneCsvWriter")
    public FlatFileItemWriter<Prenotazione> prenotazioneCsvWriter() {
        return createCsvWriter(exportFilePath);
    }

    public FlatFileItemWriter<Prenotazione> createCsvWriter(String filePath) {
        // Verifica che la directory di destinazione esista
        File file = new File(filePath);
        File parentDir = file.getParentFile();

        // Verifica se il file esiste e crealo se necessario
        try {
            if (!file.exists()) {
                boolean isFileCreated = file.createNewFile();
                if (isFileCreated) {
                    log.info("File creato con successo: {}", filePath);
                } else {
                    log.warn("Il file non è stato creato, potrebbe esistere già: {}", filePath);
                }
            } else {
                log.info("Il file esiste già: {}", filePath);
            }
        } catch (IOException e) {
            log.error("Errore durante la creazione del file: {}", e.getMessage());
            throw new IllegalStateException("Impossibile creare il file: " + filePath, e);
        }

        // Creazione del writer
        FlatFileItemWriter<Prenotazione> writer = new FlatFileItemWriter<>();
        writer.setResource(new FileSystemResource(filePath));
        log.info("Il file verrà scritto in: {}", filePath);

        // Aggiunta dell'header al file
        writer.setHeaderCallback(headerWriter -> {
            headerWriter.write("ID,UserID,VeicoloID,DataPrenotazione,DataInizio,DataFine,Note");
            log.info("Header aggiunto al file: {}", filePath);
        });

        // Configurazione del line aggregator
        DelimitedLineAggregator<Prenotazione> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(",");
        BeanWrapperFieldExtractor<Prenotazione> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[]{
                "prenotazioneId", "user.userId", "veicolo.veicoloId",
                "dataPrenotazione", "dataInizio", "dataFine", "note"
        });
        lineAggregator.setFieldExtractor(fieldExtractor);
        writer.setLineAggregator(lineAggregator);

        // Listener per tracciare il comportamento durante la scrittura
        writer.setAppendAllowed(false);
        log.info("FlatFileItemWriter configurato correttamente per il file: {}", filePath);

        return writer;
    }
}
