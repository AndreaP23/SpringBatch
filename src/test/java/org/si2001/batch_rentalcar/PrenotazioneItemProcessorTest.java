package org.si2001.batch_rentalcar;

import org.example.dao.PrenotazioneRepository;
import org.example.dao.UserRepository;
import org.example.dao.VeicoloRepository;
import org.example.entities.Prenotazione;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.si2001.batch_rentalcar.config.PrenotazioneItemProcessor;
import org.si2001.batch_rentalcar.dto.PrenotazioneDTO;
import org.si2001.batch_rentalcar.mapper.PrenotazioneMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.nio.file.*;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest // Carica l'intero contesto dell'applicazione
@ActiveProfiles("test")
@Sql(scripts = "/test_data.sql")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class PrenotazioneItemProcessorTest {

    @MockBean
    private PrenotazioneRepository prenotazioneRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private VeicoloRepository veicoloRepository;

    @MockBean
    private PrenotazioneMapper prenotazioneMapper;

    @Autowired
    @Qualifier("prenotazioneItemProcessor")
    private PrenotazioneItemProcessor processor;

    // Directory temporanea per i Test
    private Path tempDir;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this); // Inizializza i mock

        // Crea una directory temporanea per i test
        tempDir = Files.createTempDirectory("test-input");

        // Crea un file fittizio nella directory
        Files.createFile(tempDir.resolve("mock-file.csv"));
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (tempDir != null) {
            try (Stream<Path> paths = Files.walk(tempDir)) {
                paths.map(Path::toFile).forEach(file -> {
                    if (!file.delete()) {
                        file.deleteOnExit();
                    }
                });
            }
        }
    }

    @Test
    public void testProcess_UserNonEsistente() throws Exception {
        // Mock del comportamento del repository
        when(userRepository.existsById(999L)).thenReturn(false);

        PrenotazioneDTO dto = new PrenotazioneDTO();
        dto.setUserId(999L);
        dto.setVeicoloId(1L);

        Prenotazione result = processor.process(dto);
        assertNull(result, "La prenotazione deve essere ignorata se l'utente non esiste.");
    }

    @Test
    public void testProcess_VeicoloNonEsistente() throws Exception {
        // Mock del comportamento del repository
        when(userRepository.existsById(1L)).thenReturn(true);
        when(veicoloRepository.existsById(999L)).thenReturn(false);

        PrenotazioneDTO dto = new PrenotazioneDTO();
        dto.setUserId(1L);
        dto.setVeicoloId(999L);

        Prenotazione result = processor.process(dto);
        assertNull(result, "La prenotazione deve essere ignorata se il veicolo non esiste.");
    }

    @Test
    public void testProcess_DataInizioNelPassato() throws Exception {
        // Mock del comportamento del repository
        when(userRepository.existsById(1L)).thenReturn(true);
        when(veicoloRepository.existsById(2L)).thenReturn(true);

        PrenotazioneDTO dto = new PrenotazioneDTO();
        dto.setUserId(1L);
        dto.setVeicoloId(2L);
        dto.setDataInizio(LocalDate.now().minusDays(1)); // Data nel passato
        dto.setDataFine(LocalDate.now().plusDays(2));

        Prenotazione result = processor.process(dto);
        assertNull(result, "La prenotazione deve essere ignorata se la data di inizio è nel passato.");
    }

    @Test
    public void testProcess_DataFineNonValida() throws Exception {
        // Mock del comportamento del repository
        when(userRepository.existsById(1L)).thenReturn(true);
        when(veicoloRepository.existsById(2L)).thenReturn(true);

        PrenotazioneDTO dto = new PrenotazioneDTO();
        dto.setUserId(1L);
        dto.setVeicoloId(2L);
        dto.setDataInizio(LocalDate.now().plusDays(1));
        dto.setDataFine(LocalDate.now().plusDays(1)); // Data di fine troppo vicina alla data di inizio

        Prenotazione result = processor.process(dto);
        assertNull(result, "La prenotazione deve essere ignorata se la data di fine è troppo vicina alla data di inizio.");
    }
}
