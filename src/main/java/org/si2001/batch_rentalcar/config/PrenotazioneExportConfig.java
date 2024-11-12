package org.si2001.batch_rentalcar.config;

import lombok.extern.slf4j.Slf4j;
import org.example.entities.Prenotazione;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

@Slf4j
@Configuration
public class PrenotazioneExportConfig {

    @Bean
    public FlatFileItemWriter<Prenotazione> prenotazioneCsvWriter() {
        return createCsvWriter("output/prenotazioni_export.csv");
    }

    public FlatFileItemWriter<Prenotazione> createCsvWriter(String filePath) {
        FlatFileItemWriter<Prenotazione> writer = new FlatFileItemWriter<>();
        writer.setResource(new FileSystemResource(filePath));

        writer.setHeaderCallback(headerWriter -> {
            headerWriter.write("ID,UserID,VeicoloID,DataPrenotazione,DataInizio,DataFine,Note");
        });

        DelimitedLineAggregator<Prenotazione> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(",");
        BeanWrapperFieldExtractor<Prenotazione> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[]{
                "prenotazioneId", "user.userId", "veicolo.veicoloId",
                "dataPrenotazione", "dataInizio", "dataFine", "note"
        });
        lineAggregator.setFieldExtractor(fieldExtractor);
        writer.setLineAggregator(lineAggregator);

        return writer;
    }
}
