package com.kindtail.adoptmate.batch;

import com.kindtail.adoptmate.adoption.domain.Adoption;
import com.kindtail.adoptmate.adoption.domain.AdoptionStatus;
import com.kindtail.adoptmate.animal.domain.Status;
import com.kindtail.adoptmate.animal.dto.AnimalStatusUpdateRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 14일 이상 대기(PENDING) 상태로 방치된 입양 신청 건을
 * 자동으로 반려(REJECTED) 처리하고, 동물 상태를 다시 보호중(PROTECTED)으로 원복시키는 배치 Job 설정
 */
@Configuration
public class ExpiredAdoptionBatchConfig {

    private static final int CHUNK_SIZE = 100;

    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    public Job expiredAdoptionJob(JobRepository jobRepository, Step expiredAdoptionStep) {
        return new JobBuilder("expiredAdoptionJob", jobRepository)
                .start(expiredAdoptionStep)
                .build();
    }

    @Bean
    public Step expiredAdoptionStep(JobRepository jobRepository,
                                    PlatformTransactionManager transactionManager,
                                    JpaPagingItemReader<Adoption> expiredAdoptionReader,
                                    ItemProcessor<Adoption, Adoption> expiredAdoptionProcessor,
                                    ItemWriter<Adoption> expiredAdoptionWriter) {
        return new StepBuilder("expiredAdoptionStep", jobRepository)
                .<Adoption, Adoption>chunk(CHUNK_SIZE, transactionManager)
                .reader(expiredAdoptionReader)
                .processor(expiredAdoptionProcessor)
                .writer(expiredAdoptionWriter)
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<Adoption> expiredAdoptionReader(
            EntityManagerFactory emf,
            @Value("#{jobParameters['thresholdDate']}") String thresholdDateStr) {

        LocalDateTime thresholdDate = (thresholdDateStr != null && !thresholdDateStr.isBlank())
                ? LocalDateTime.parse(thresholdDateStr)
                : LocalDateTime.now().minusDays(14);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("status", AdoptionStatus.PENDING);
        parameters.put("thresholdDate", thresholdDate);

        return new JpaPagingItemReaderBuilder<Adoption>()
                .name("expiredAdoptionReader")
                .entityManagerFactory(emf)
                .queryString("SELECT a FROM Adoption a JOIN FETCH a.animal WHERE a.status = :status AND a.applyDate <= :thresholdDate ORDER BY a.id ASC")
                .parameterValues(parameters)
                .pageSize(CHUNK_SIZE)
                .build();
    }

    @Bean
    public ItemProcessor<Adoption, Adoption> expiredAdoptionProcessor() {
        return adoption -> {
            adoption.updateAdoption(AdoptionStatus.REJECTED);
            if (adoption.getAnimal() != null) {
                adoption.getAnimal().updateStatus(new AnimalStatusUpdateRequest(Status.PROTECTED));
            }
            return adoption;
        };
    }

    @Bean
    public ItemWriter<Adoption> expiredAdoptionWriter() {
        return chunk -> {
            for (Adoption adoption : chunk) {
                entityManager.merge(adoption);
                if (adoption.getAnimal() != null) {
                    entityManager.merge(adoption.getAnimal());
                }
            }
        };
    }
}
