package org.si2001.batch_rentalcar;

import lombok.extern.slf4j.Slf4j;
import org.example.dao.PrenotazioneRepository;
import org.example.entities.Prenotazione;
import org.example.entities.User;
import org.example.entities.Veicolo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.si2001.batch_rentalcar.config.JpaConfig;
import org.si2001.batch_rentalcar.config.PrenotazioneExportConfig;
import org.si2001.batch_rentalcar.config.PrenotazioniBatchConfig;
import org.si2001.batch_rentalcar.config.TestBatchConfig;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")  // Assicurati che venga usato il profilo di test
@Slf4j
@SpringBootTest(classes = {
        PrenotazioniBatchConfig.class,
        TestBatchConfig.class,
        JpaConfig.class,
        PrenotazioneExportConfig.class
})
@SpringBatchTest
@Sql({"/test_data.sql", "classpath:batch_tables_h2.sql"})
@EnableJpaRepositories(basePackages = "org.example.dao")
public class PrenotazioneExportJobTest {

    @Value("${prenotazione.export.path}")
    private String outputFilePath;

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    @Qualifier("exportPrenotazioniJob")
    private Job prenotazioneExportJob;

    @Autowired
    private PrenotazioneRepository prenotazioneRepository;

    @BeforeEach
    public void setUp() throws Exception {
        log.info("Set up del test in corso...");
        jobLauncherTestUtils.setJob(prenotazioneExportJob);

        // Percorso del file di output
        Path outputPath = Paths.get(outputFilePath);

        // Crea la directory di output se non esiste
        Files.createDirectories(outputPath.getParent());
        log.debug("Directory per il file di output creata o già esistente: {}", outputPath.getParent());

        log.info("Percorso del file di output configurato: {}", outputFilePath);

        configureTestData();
    }

    // Verificare con un altro mock
    private void configureTestData() {
        User mockUser = new User();
        mockUser.setUserId(2L);
        mockUser.setNome("Mario");
        mockUser.setCognome("Rossi");

        Veicolo mockVeicolo = new Veicolo();
        mockVeicolo.setVeicoloId(3L);
        mockVeicolo.setMarca("Fiat");
        mockVeicolo.setModello("Panda");

        Prenotazione mockPrenotazione = new Prenotazione();
        mockPrenotazione.setPrenotazioneId(1L);
        mockPrenotazione.setUser(mockUser);
        mockPrenotazione.setVeicolo(mockVeicolo);
        mockPrenotazione.setDataPrenotazione(LocalDate.now());
        mockPrenotazione.setDataInizio(LocalDate.now().plusDays(1));
        mockPrenotazione.setDataFine(LocalDate.now().plusDays(5));
        mockPrenotazione.setNote("Prenotazione per viaggio di lavoro");

        // Mock prenotazione con data passata
        User mockUser2 = new User();
        mockUser2.setUserId(3L);
        mockUser2.setNome("Giulia");
        mockUser2.setCognome("Verdi");

        Veicolo mockVeicolo2 = new Veicolo();
        mockVeicolo2.setVeicoloId(4L);
        mockVeicolo2.setMarca("Renault");
        mockVeicolo2.setModello("Clio");

        Prenotazione mockPrenotazione2 = new Prenotazione();
        mockPrenotazione2.setPrenotazioneId(2L);
        mockPrenotazione2.setUser(mockUser2);
        mockPrenotazione2.setVeicolo(mockVeicolo2);
        mockPrenotazione2.setDataPrenotazione(LocalDate.now().minusDays(5)); // Data passata
        mockPrenotazione2.setDataInizio(LocalDate.now().minusDays(4));
        mockPrenotazione2.setDataFine(LocalDate.now().minusDays(1));
        mockPrenotazione2.setNote("Prenotazione scaduta");


        // Salva i dati nel repository per il test
        prenotazioneRepository.save(mockPrenotazione);
        prenotazioneRepository.save(mockPrenotazione2);
    }


    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    public void testExportPrenotazioniJob_CreatesOutputFile() throws Exception {
        log.info("Esecuzione del job di esportazione prenotazioni...");

        assertNotNull(jobLauncherTestUtils.getJob(), "Il job non è stato caricato correttamente.");

        JobParameters jobParameters = new JobParametersBuilder()
                .addDate("timestamp", java.util.Calendar.getInstance().getTime())
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.getJobLauncher()
                .run(jobLauncherTestUtils.getJob(), jobParameters);

        log.debug("Job eseguito, stato del job: {}", jobExecution.getExitStatus());
        assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus(), "Il job non è stato completato con successo.");

        // Verifica che il file di output sia stato creato
        verifyOutputFile(outputFilePath);

        log.info("Esecuzione del job di esportazione prenotazioni completata con successo.");
    }


    private void verifyOutputFile(String filePath) throws Exception {
        File outputFile = new File(filePath);

        assertTrue(outputFile.exists(), "Il file di output non è stato creato.");
        log.info("Il file di output è stato creato correttamente: {}", filePath);

        // Leggi e verifica il contenuto del file CSV
        try (BufferedReader reader = new BufferedReader(new FileReader(outputFile))) {
            // Verifica l'header
            String header = reader.readLine();
            assertEquals("ID,UserID,VeicoloID,DataPrenotazione,DataInizio,DataFine,Note", header, "L'header del file CSV non corrisponde.");

            // Verifica i dati
            String firstLine = reader.readLine();
            assertNotNull(firstLine, "Il file CSV non contiene dati.");
            String[] firstLineTokens = firstLine.split(",");
            assertEquals("2", firstLineTokens[1], "L'ID utente non è corretto.");
            assertEquals("3", firstLineTokens[2], "L'ID veicolo non è corretto.");
            assertEquals(LocalDate.now().toString(), firstLineTokens[3], "La data di prenotazione non è corretta.");
            assertEquals(LocalDate.now().plusDays(1).toString(), firstLineTokens[4], "La data di inizio non è corretta.");
            assertEquals(LocalDate.now().plusDays(5).toString(), firstLineTokens[5], "La data di fine non è corretta.");
            assertEquals("Prenotazione per viaggio di lavoro", firstLineTokens[6], "Le note non sono corrette.");
        }
    }

    /*@AfterEach
    public void cleanUp() throws Exception {
        log.info("Pulizia: rimozione del file di output...");

        Path outputPath = Paths.get(outputFilePath);
        if (Files.exists(outputPath)) {
            Files.delete(outputPath);
            log.info("File di output eliminato correttamente: {}", outputPath);
        } else {
            log.warn("File di output non trovato: {}", outputPath);
        }
    }*/

}
