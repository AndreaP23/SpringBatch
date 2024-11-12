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
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@EnableBatchProcessing
public class PrenotazioniBatchConfig {

    private static final String OUTPUT_DIRECTORY = "src/main/resources/output";
    private static final String OUTPUT_FILE = OUTPUT_DIRECTORY + "/prenotazioniscaricate.csv";

    @Autowired
    private PrenotazioneExportConfig prenotazioneExportConfig;

    /**
     * Job per l'importazione delle prenotazioni dal CSV.
     */
    @Bean
    public Job importPrenotazioniJob(JobRepository jobRepository,
                                     PlatformTransactionManager transactionManager,
                                     PrenotazioneRepository prenotazioneRepository,
                                     PrenotazioneMapper prenotazioneMapper,
                                     UserRepository userRepository,
                                     VeicoloRepository veicoloRepository) {
        return new JobBuilder("importPrenotazioniJob", jobRepository)
                .start(importPrenotazioniStep(jobRepository, transactionManager, prenotazioneMapper, userRepository, veicoloRepository, prenotazioneRepository))
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
                                       PrenotazioneRepository prenotazioneRepository) {
        return new StepBuilder("importPrenotazioniStep", jobRepository)
                .<PrenotazioneDTO, Prenotazione>chunk(100, transactionManager)
                .reader(flatFileItemReader(null))
                .processor(importPrenotazioneProcessor(prenotazioneMapper, userRepository, veicoloRepository, prenotazioneRepository))
                .writer(importPrenotazioneWriter(prenotazioneRepository))
                .listener(importItemReadListener())
                .build();
    }


    /**
     * Job per l'esportazione delle prenotazioni in un file CSV.
     */
    @Bean
    public Job exportPrenotazioniJob(JobRepository jobRepository,
                                     PlatformTransactionManager transactionManager,
                                     PrenotazioneRepository prenotazioneRepository) {
        return new JobBuilder("exportPrenotazioniJob", jobRepository)
                .start(exportPrenotazioniStep(jobRepository, transactionManager, prenotazioneRepository))
                .build();
    }

    /**
     * Step per l'esportazione delle prenotazioni.
     */
    @Bean
    public Step exportPrenotazioniStep(JobRepository jobRepository,
                                       PlatformTransactionManager transactionManager,
                                       PrenotazioneRepository prenotazioneRepository) {
        FlatFileItemWriter<Prenotazione> writer = prenotazioneExportConfig.prenotazioneCsvWriter();
        writer.setResource(new FileSystemResource(OUTPUT_FILE));
        return new StepBuilder("exportPrenotazioniStep", jobRepository)
                .<Prenotazione, Prenotazione>chunk(10, transactionManager)
                .reader(repositoryItemReader(prenotazioneRepository))
                .writer(writer)
                .build();
    }

    /**
     * Configurazione del Reader dal database.
     */
    @Bean(name = "repositoryPrenotazioniItemReader")
    @StepScope
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


    /**
     * Configurazione del Writer per l'importazione.
     */
    @Bean
    public PrenotazioneItemWriter importPrenotazioneWriter(PrenotazioneRepository prenotazioneRepository) {
        return new PrenotazioneItemWriter(prenotazioneRepository);
    }

    /**
     * Configurazione del Processor per l'importazione.
     */
    @Bean
    public ItemProcessor<PrenotazioneDTO, Prenotazione> importPrenotazioneProcessor(PrenotazioneMapper prenotazioneMapper,
                                                                                    UserRepository userRepository,
                                                                                    VeicoloRepository veicoloRepository,
                                                                                    PrenotazioneRepository prenotazioneRepository) {
        return new PrenotazioneItemProcessor(prenotazioneMapper, userRepository, veicoloRepository, prenotazioneRepository);
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
     * Configurazione del Reader per il CSV.
     */
    @Bean
    @StepScope
    public FlatFileItemReader<PrenotazioneDTO> flatFileItemReader(@Value("#{jobParameters['inputFile']}") String prenotazioniFile) {
        FlatFileItemReader<PrenotazioneDTO> reader = new FlatFileItemReader<>();
        log.info("Inizializzazione del Reader per il file: {}", prenotazioniFile);
        reader.setName("PRENOTAZIONI_READER");
        reader.setLinesToSkip(1);
        reader.setLineMapper(lineMapper());
        reader.setStrict(false);
        reader.setResource(new FileSystemResource(prenotazioniFile));
        return reader;
    }

    /**
     * Configurazione del LineMapper per il CSV.
     */
    @Bean
    public DefaultLineMapper<PrenotazioneDTO> lineMapper() {
        DefaultLineMapper<PrenotazioneDTO> lineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(",");
        tokenizer.setNames("userId", "veicoloId", "dataPrenotazione", "dataInizio", "dataFine", "note");
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(new PrenotazioneFieldSetMapper());
        return lineMapper;
    }
}
