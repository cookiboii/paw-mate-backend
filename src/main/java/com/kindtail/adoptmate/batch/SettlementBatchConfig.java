package com.kindtail.adoptmate.batch;

import com.kindtail.adoptmate.adoption.domain.Adoption;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class SettlementBatchConfig {

    private static final int CHUNK_SIZE = 1000;

    @Bean
    public Job offsetJob(JobRepository jobRepository, Step offsetStep) {
        return new JobBuilder("offsetJob", jobRepository)
                .start(offsetStep)
                .build();
    }

    @Bean
    public Job zeroOffsetJob(JobRepository jobRepository, Step zeroOffsetStep) {
        return new JobBuilder("zeroOffsetJob", jobRepository)
                .start(zeroOffsetStep)
                .build();
    }

    @Bean
    public Step offsetStep(JobRepository jobRepository, 
                           PlatformTransactionManager transactionManager, 
                           EntityManagerFactory emf) {
        return new StepBuilder("offsetStep", jobRepository)
                .<Adoption, Adoption>chunk(CHUNK_SIZE, transactionManager)
                .reader(offsetItemReader(emf))
                .writer(dummyWriter())
                .build();
    }

    @Bean
    public Step zeroOffsetStep(JobRepository jobRepository, 
                               PlatformTransactionManager transactionManager, 
                               EntityManagerFactory emf) {
        return new StepBuilder("zeroOffsetStep", jobRepository)
                .<Adoption, Adoption>chunk(CHUNK_SIZE, transactionManager)
                .reader(new ZeroOffsetAdoptionReader(emf, CHUNK_SIZE))
                .writer(dummyWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<Adoption> offsetItemReader(EntityManagerFactory emf) {
        return new JpaPagingItemReaderBuilder<Adoption>()
                .name("offsetItemReader")
                .entityManagerFactory(emf)
                .queryString("SELECT a FROM Adoption a ORDER BY a.id ASC")
                .pageSize(CHUNK_SIZE)
                .build();
    }

    @Bean
    public ItemWriter<Adoption> dummyWriter() {
        return chunk -> {
            // I/O 측정에 집중하기 위해 단순 소비
        };
    }
}
