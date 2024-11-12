package org.si2001.batch_rentalcar;

import org.example.dao.PrenotazioneRepository;
import org.example.entities.Prenotazione;
import org.example.entities.User;
import org.example.entities.Veicolo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.MapperConfig;
import org.si2001.batch_rentalcar.config.JpaConfig;
import org.si2001.batch_rentalcar.config.PrenotazioneExportConfig;
import org.si2001.batch_rentalcar.config.PrenotazioniBatchConfig;
import org.si2001.batch_rentalcar.config.TestBatchConfig;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@SpringBootTest(classes = {
        PrenotazioniBatchConfig.class,
        TestBatchConfig.class,
        JpaConfig.class,
        PrenotazioneExportConfig.class,
        MapperConfig.class
})
@SpringBatchTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestPropertySource(locations = "classpath:application-test.properties")
@Sql("/test_data.sql")
public class PrenotazioneExportJobTest {

    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
    private static final String OUTPUT_FILE = TEMP_DIR + "/prenotazioniscaricate.csv";

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    @Qualifier("exportPrenotazioniJob")
    private Job prenotazioneExportJob;

    @MockBean
    private PrenotazioneRepository prenotazioneRepository;

    @BeforeEach
    public void setUp() throws Exception {
        jobLauncherTestUtils.setJob(prenotazioneExportJob);

        // Elimina il file temporaneo se esiste
        Path outputPath = Paths.get(OUTPUT_FILE);
        if (Files.exists(outputPath)) {
            Files.delete(outputPath);
        }

        // Definisci il comportamento del mock
        User mockUser = User.builder()
                .userId(2L)
                .nome("Mario")
                .cognome("Rossi")
                .build();

        Veicolo mockVeicolo = Veicolo.builder()
                .veicoloId(3L)
                .marca("Fiat")
                .modello("Panda")
                .build();

        Prenotazione mockPrenotazione = Prenotazione.builder()
                .prenotazioneId(1L)
                .user(mockUser)
                .veicolo(mockVeicolo)
                .dataPrenotazione(LocalDate.now())
                .dataInizio(LocalDate.now().plusDays(1))
                .dataFine(LocalDate.now().plusDays(5))
                .note("Prenotazione di Mario Rossi")
                .build();

        List<Prenotazione> mockPrenotazioni = Collections.singletonList(mockPrenotazione);
        Page<Prenotazione> mockPagePrenotazioni = new PageImpl<>(mockPrenotazioni);

        // Mock del metodo findAllByToday per restituire un Page di prenotazioni
        when(prenotazioneRepository.findAllByToday(any(Pageable.class))).thenReturn(mockPagePrenotazioni);
    }

    @AfterEach
    public void tearDown() throws Exception {
        // Elimina il file di output generato dal test
        Path outputPath = Paths.get(OUTPUT_FILE);
        if (Files.exists(outputPath)) {
            Files.delete(outputPath);
        }
    }

    @Test
    @Sql({"/test_data.sql"})
    public void testExportPrenotazioniJob_CreatesOutputFile() throws Exception {
        assertNotNull(jobLauncherTestUtils.getJob(), "Il job non è stato caricato correttamente.");

        // Esecuzione del job con parametri appropriati
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("outputFile", OUTPUT_FILE)
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.getJobLauncher()
                .run(jobLauncherTestUtils.getJob(), jobParameters);

        // Verifica che il job sia stato completato con successo
        assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus(), "Il job non è stato completato con successo.");

        // Verifica che il file di output sia stato creato
        File outputFile = new File(OUTPUT_FILE);
        assertTrue(outputFile.exists(), "Il file di output non è stato creato.");

        // Verifica il contenuto del file di output
        try (BufferedReader reader = new BufferedReader(new FileReader(outputFile))) {
            String header = reader.readLine();
            assertEquals("userId,veicoloId,dataPrenotazione,dataInizio,dataFine,note", header, "L'header del file CSV non corrisponde.");

            String firstLine = reader.readLine();
            assertNotNull(firstLine, "Il file CSV non contiene dati.");

            // Verifica dettagliata del contenuto
            String[] firstLineTokens = firstLine.split(",");
            assertEquals("2", firstLineTokens[0], "L'ID utente non è corretto.");
            assertEquals("3", firstLineTokens[1], "L'ID veicolo non è corretto.");
            assertEquals(LocalDate.now().toString(), firstLineTokens[2], "La data di prenotazione non è corretta.");
            assertEquals(LocalDate.now().plusDays(1).toString(), firstLineTokens[3], "La data di inizio non è corretta.");
            assertEquals(LocalDate.now().plusDays(5).toString(), firstLineTokens[4], "La data di fine non è corretta.");
            assertEquals("Prenotazione di Mario Rossi", firstLineTokens[5], "Le note non sono corrette.");
        }

        // Verifica se i dati sono stati correttamente persistiti nel mock del repository
        assertEquals(1, prenotazioneRepository.findAllByToday(Pageable.unpaged()).getTotalElements(), "Il numero di prenotazioni persistite non è corretto.");
    }
}
