package org.si2001.batch_rentalcar;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "org.si2001.batch_rentalcar")
@ComponentScan(basePackages = {
        "org.si2001.batch_rentalcar",
        "org.example",
        "org.si2001.batch_rentalcar.mapper"
})
@EnableJpaRepositories(basePackages = "org.example.dao")
@EntityScan(basePackages = "org.example.entities")
@Slf4j

public class BatchRentalcarApplication {

    private final JobLauncher jobLauncher;
    private final Job importJob;
    private final Job exportJob;

    // Utilizziamo Qualifier per specificare quale Bean deve essere iniettato
    public BatchRentalcarApplication(JobLauncher jobLauncher,
                                     @Qualifier("importPrenotazioniJob") Job importJob,
                                     @Qualifier("exportPrenotazioniJob") Job exportJob) {
        this.jobLauncher = jobLauncher;
        this.importJob = importJob;
        this.exportJob = exportJob;
    }

    public static void main(String[] args) {
        ApplicationContext context = new SpringApplicationBuilder(BatchRentalcarApplication.class)
                .web(WebApplicationType.SERVLET)
                .run(args);

        BatchRentalcarApplication application = context.getBean(BatchRentalcarApplication.class);

        // Avvia manualmente il job di importazione (se richiesto)
        application.runImportJob("src/main/resources/input/prenotazioni.csv");

        // Avvia manualmente il job di esportazione
        application.runExportJob();
    }

    /**
     * Avvia il job di importazione per un file specifico.
     */
    public void runImportJob(String inputFile) {
        try {
            log.info("Avvio del job di importazione per il file: {}", inputFile);
            val jobParameters = new JobParametersBuilder()
                    .addDate("timestamp", java.util.Calendar.getInstance().getTime())
                    .addString("inputFile", inputFile)
                    .toJobParameters();

            val jobExecution = jobLauncher.run(importJob, jobParameters);

            while (jobExecution.isRunning()) {
                log.info("Il job di importazione è in esecuzione...");
                Thread.sleep(1000);
            }

            log.info("Job di importazione completato con stato: {}", jobExecution.getStatus());
        } catch (Exception e) {
            log.error("Errore durante l'esecuzione del job di importazione.", e);
        }
    }

    /**
     * Avvia il job di esportazione per scrivere i dati dal database a un file CSV.
     */
    public void runExportJob() {
        try {
            log.info("Avvio del job di esportazione.");
            val jobParameters = new JobParametersBuilder()
                    .addDate("timestamp", java.util.Calendar.getInstance().getTime())
                    .toJobParameters();

            val jobExecution = jobLauncher.run(exportJob, jobParameters);

            while (jobExecution.isRunning()) {
                log.info("Il job di esportazione è in esecuzione...");
                Thread.sleep(1000);
            }

            log.info("Job di esportazione completato con stato: {}", jobExecution.getStatus());
        } catch (Exception e) {
            log.error("Errore durante l'esecuzione del job di esportazione.", e);
        }
    }

}
