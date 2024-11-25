package org.si2001.batch_rentalcar.config;

import lombok.extern.slf4j.Slf4j;
import org.example.dao.PrenotazioneRepository;
import org.example.dao.UserRepository;
import org.example.dao.VeicoloRepository;
import org.example.entities.Prenotazione;
import org.si2001.batch_rentalcar.dto.PrenotazioneDTO;
import org.si2001.batch_rentalcar.mapper.PrenotazioneMapper;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Slf4j
@Configuration
@EnableBatchProcessing
public class PrenotazioniBatchConfig {

    @Value("${prenotazione.export.path}")
    private String exportFilePath;

    @Autowired
    @Qualifier("flatFileItemReader")
    private FlatFileItemReader<PrenotazioneDTO> prenotazioneItemReader;

    @Autowired
    @Qualifier("repositoryPrenotazioniItemReader")
    private RepositoryItemReader<Prenotazione> prenotazioneRepositoryReader;

    @Autowired
    private ItemProcessor<PrenotazioneDTO, Prenotazione> importPrenotazioneProcessor;

    @Autowired
    @Qualifier("prenotazioneCsvWriter")
    private ItemWriter<Prenotazione> prenotazioneCsvWriter;


    @Autowired
    private ItemProcessor<Prenotazione, Prenotazione> repositoryItemProcessor;

    /**
     * Job per l'importazione delle prenotazioni dal CSV.
     */
    @Bean
    public Job importPrenotazioniJob(JobRepository jobRepository,
                                     PlatformTransactionManager transactionManager,
                                     PrenotazioneRepository prenotazioneRepository,
                                     PrenotazioneMapper prenotazioneMapper,
                                     UserRepository userRepository,
                                     VeicoloRepository veicoloRepository,
                                     PrenotazioneItemWriter prenotazioneItemWriter) {
        return new JobBuilder("importPrenotazioniJob", jobRepository)
                .start(importPrenotazioniStep(jobRepository, transactionManager, prenotazioneMapper, userRepository, veicoloRepository, prenotazioneRepository, prenotazioneItemWriter))
                .build();
    }



    /**
     * Step per l'importazione delle prenotazioni.
     */
    @Bean
    public Step importPrenotazioniStep(JobRepository jobRepository,
                                       PlatformTransactionManager transactionManager,
                                       PrenotazioneMapper prenotazioneMapper,
                                       UserRepository userRepository,
                                       VeicoloRepository veicoloRepository,
                                       PrenotazioneRepository prenotazioneRepository,
                                       PrenotazioneItemWriter prenotazioneItemWriter) {
        return new StepBuilder("importPrenotazioniStep", jobRepository)
                .<PrenotazioneDTO, Prenotazione>chunk(100, transactionManager)
                .reader(prenotazioneItemReader) // Legge i dati dal CSV
                .processor(importPrenotazioneProcessor) // Processa i dati
                .writer(prenotazioneItemWriter) // Scrive i dati nel database
                .listener(importItemReadListener()) // Aggiunge un listener per il monitoraggio
                .build();
    }


    /**
     * Job per l'esportazione delle prenotazioni in un file CSV.
     */
    @Bean
    public Job exportPrenotazioniJob(JobRepository jobRepository,
                                     PlatformTransactionManager transactionManager,
                                     Step exportPrenotazioniStep) {
        log.info("Configurazione del job di esportazione...");
        return new JobBuilder("exportPrenotazioniJob", jobRepository)
                .start(exportPrenotazioniStep)
                .build();
    }



    /**
     * Step per l'esportazione delle prenotazioni.
     */
    @Bean
    public Step exportPrenotazioniStep(JobRepository jobRepository,
                                       PlatformTransactionManager transactionManager,
                                       RepositoryItemReader<Prenotazione> prenotazioneRepositoryReader,
                                       ItemProcessor<Prenotazione, Prenotazione> repositoryItemProcessor,
                                       FlatFileItemWriter<Prenotazione> prenotazioneCsvWriter) {
        return new StepBuilder("exportPrenotazioniStep", jobRepository)
                .<Prenotazione, Prenotazione>chunk(10, transactionManager)
                .reader(prenotazioneRepositoryReader) // Legge dal database
                .writer(prenotazioneCsvWriter)       // Scrive sul file CSV
                .build();
    }


    /**
     * Listener per i log del Reader.
     */
    @Bean
    public ItemReadListener<PrenotazioneDTO> importItemReadListener() {
        return new ItemReadListener<>() {
            @Override
            public void beforeRead() {
                log.info("Inizio lettura del prossimo elemento...");
            }

            @Override
            public void afterRead(PrenotazioneDTO item) {
                log.info("Elemento letto dal file: {}", item);
            }

            @Override
            public void onReadError(Exception ex) {
                log.error("Errore durante la lettura dell'elemento", ex);
            }
        };
    }

    /**
     * Metodo per leggere e loggare il contenuto del file esportato.
     */
    private void logFileContent() {
        File file = new File(exportFilePath);
        log.info("Verifica del file: {}", file.getAbsolutePath());

        if (!file.exists()) {
            log.error("Il file {} non esiste.", file.getAbsolutePath());
            return;
        }

        try {
            List<String> lines = Files.readAllLines(file.toPath());
            log.info("Contenuto del file {}:", file.getAbsolutePath());
            for (String line : lines) {
                log.info(line);
            }
        } catch (IOException e) {
            log.error("Errore durante la lettura del file {}", file.getAbsolutePath(), e);
        }
    }
}
