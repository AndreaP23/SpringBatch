package org.si2001.batch_rentalcar;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication(scanBasePackages = "org.si2001.batch_rentalcar")
@ComponentScan(basePackages = {
        "org.si2001.batch_rentalcar",
        "org.example",
        "org.si2001.batch_rentalcar.mapper",
        "org.si2001.batch_rentalcar.config"
})

@EnableJpaRepositories(basePackages = "org.example.dao")
@EntityScan(basePackages = "org.example.entities")
@Slf4j
public class BatchRentalcarApplication {

    private final JobLauncher jobLauncher;
    private final Map<String, Job> jobs = new HashMap<>();

    /**
     * Costruttore per inizializzare i job disponibili.
     */

    public BatchRentalcarApplication(JobLauncher jobLauncher,
                                     @Qualifier("importPrenotazioniJob") Job importJob,
                                     @Qualifier("exportPrenotazioniJob") Job exportJob) {
        this.jobLauncher = jobLauncher;
        this.jobs.put("import", importJob);
        this.jobs.put("export", exportJob);
        //put.all inserisco tutti
    }

    public static void main(String[] args) {
        ApplicationContext context = new SpringApplicationBuilder(BatchRentalcarApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);

        BatchRentalcarApplication application = context.getBean(BatchRentalcarApplication.class);
        Environment env = context.getEnvironment();

        //Questo controllo non serve
        // Determina il tipo di job dal primo argomento o dalla proprietà
        String jobType = args.length > 0 ? args[0] : env.getProperty("batch.default.job", "import");

        //Anche questo da togliere, lo ho già nei passaggi
        String inputFile = env.getProperty("prenotazione.import.path");

        application.runJob(jobType, inputFile);

    }

    /**
     * Esegue il job specificato con eventuali parametri.
     *
     * @param jobType   Il tipo di job da eseguire ("import" o "export").
     * @param inputFile Percorso del file di input (necessario solo per il job "import").
     */
    public void runJob(String jobType, String inputFile) {
        Job job = jobs.get(jobType.toLowerCase());

        if (job == null) {
            log.error("Tipo di job non valido: {}. Usare 'import' o 'export'.", jobType);
            return;
        }

        try {
            log.info("Avvio del job di tipo: {}", jobType);

            // Crea i parametri per il job
            JobParametersBuilder parametersBuilder = new JobParametersBuilder()
                    .addDate("timestamp", java.util.Calendar.getInstance().getTime());

            if ("import".equalsIgnoreCase(jobType) && inputFile != null) {
                parametersBuilder.addString("inputFile", inputFile);
            }

            JobParameters jobParameters = parametersBuilder.toJobParameters();

            // Esegui il job
            JobExecution jobExecution = jobLauncher.run(job, jobParameters);

            while (jobExecution.isRunning()) {
                log.info("Il job '{}' è in esecuzione...", jobType);
                Thread.sleep(1000);
            }

            log.info("Job '{}' completato con stato: {}", jobType, jobExecution.getStatus());
        } catch (Exception e) {
            log.error("Errore durante l'esecuzione del job '{}'.", jobType, e);
        }
    }


}
