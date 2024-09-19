package com.batch.settle.job;

import com.batch.settle.domain.ApiOrderHistory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.PathResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class HistoryGeneratePartitionJobConfiguration {
    private final JobRepository jobRepository;

    private final PlatformTransactionManager transactionManager;

    /**
     *
     * totalCount를 gridSize로 나누어 각각의 Step에서 데이터를 읽고 파일로 쓰도록 구성
     *  totalCount = 5000000, gridSize = 7
     *  각 스텝은 5000000 / 7 = 714285개의 데이터를 읽게 되며 714285를 chunkSize만큼 나눠서 처리됨.
     *
     */

    @Bean
    public Job historyGeneratorJob(Step stepManager) {
        return new JobBuilder("historyGeneratorJob", jobRepository)
            .start(stepManager)
            .validator(
                new DefaultJobParametersValidator(
                    new String[]{"targetDate", "totalCount"},
                    new String[0]
                )
            )
            .build();
    }

    @Bean
    @JobScope
    public Step stepManager(PartitionHandler partitionHandler,
                            @Value("#{jobParameters['targetDate']}") String targetDate,
                            Step historyGenerateStep) {
        return new StepBuilder("stepManager", jobRepository)
            .partitioner("delegateStep", partitioner(targetDate))
            .step(historyGenerateStep)
            .partitionHandler(partitionHandler)
            .build();
    }

    // StepManager가 워커 스텝을 어떻게 다룰지 정의
    @Bean
    public PartitionHandler partitionHandler(Step historyGenerateStep) {
        var partitionHandler = new TaskExecutorPartitionHandler();
        partitionHandler.setStep(historyGenerateStep);
        partitionHandler.setGridSize(7);
        partitionHandler.setTaskExecutor(new SimpleAsyncTaskExecutor());
        return partitionHandler;
    }

    // 워커 스텝을 위해 StepExecution을 생성하는 인터페이스
    private Partitioner partitioner(String targetDate) {
        var formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        var date = LocalDate.parse(targetDate, formatter);

        return gridSize -> {
            var result = new HashMap<String, ExecutionContext>();
            IntStream.range(0, gridSize)
                .forEach(
                    is -> {
                        var context = new ExecutionContext();
                        context.put("targetDate", date.minusDays(is).format(formatter));
                        result.put("partition" + is, context);
                    }
                );
            return result;
        };
    }

    @Bean
    public Step historyGenerateStep(HistoryGenerateReader reader,
                                    HistoryGenerateProcessor processor) {
        return new StepBuilder("historyGenerateStep", jobRepository)
            .<Boolean, ApiOrderHistory>chunk(5000, transactionManager)
            .reader(reader)
            .processor(processor)
            .writer(historyFlatFileItemWriter(null))
            .build();
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<ApiOrderHistory> historyFlatFileItemWriter(@Value("#{stepExecutionContext['targetDate']}") String targetDate) {
        final String fileName = "order-history-" + targetDate + ".csv";

        return new FlatFileItemWriterBuilder<ApiOrderHistory>()
            .name("historyFlatFileItemWriter")
            .resource(new PathResource("src/main/resources/data/" + fileName))
            .delimited()
            .names("id", "userId", "url", "fee", "state", "createdAt")
            .headerCallback(writer -> writer.write("id,userId,url,fee,state,createdAt"))
            .build();
    }
}
