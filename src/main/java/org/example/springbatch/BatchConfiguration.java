package org.example.springbatch;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private static final String CSV_FILE = "output.csv";

    @Autowired
    @Lazy
    public BatchConfiguration(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }

   @Bean
   public ItemReader<Book> reader(EntityManagerFactory entityManagerFactory) {
       JpaPagingItemReader<Book> reader = new JpaPagingItemReader<>();
       try {
           reader.setEntityManagerFactory(entityManagerFactory);
           reader.setQueryString("SELECT b FROM Book b"); // Use the entity name 'Book'
           reader.setPageSize(10);
       } catch (Exception e) {
           System.out.println("Error " + e.getMessage());
           e.printStackTrace();
       }
       return reader;
   }


    @Bean
    public ItemProcessor<Book, Book> processor() {
        return new BookEntityItemProcessor();
    }

    @Bean
    public FlatFileItemWriter<Book> writer() {
        File file = new File(CSV_FILE);
        if (!file.exists()) {
            try {
                file.createNewFile();
            }
            catch (Exception e) {
                throw new RuntimeException(
                        "Error creating CSV file", e);
            }
        }
        FlatFileItemWriter<Book> writer = new FlatFileItemWriter<>();
        writer.setResource(new FileSystemResource(CSV_FILE));

        DelimitedLineAggregator<Book> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(",");

        BeanWrapperFieldExtractor<Book> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[] {"id", "author", "name", "price"});
        lineAggregator.setFieldExtractor(fieldExtractor);

        writer.setLineAggregator(lineAggregator);

        return writer;
    }


    @Bean
    public Job exportJob(Step exportStep) {
        Job job = null;
        try {
            job = new JobBuilder("exportJob", jobRepository)
                    .incrementer(new RunIdIncrementer())
                    .start(exportStep)
                    .build();
        } catch (Exception e) {
            System.out.println("Error " + e.getMessage());
        }
        return job;
    }

    @Bean
    public Step exportStep(ItemReader<Book> reader, ItemProcessor<Book, Book> processor, ItemWriter<Book> writer) {
        Step step = null;
        try {
            step = new StepBuilder("exportStep", jobRepository)
                    .<Book, Book>chunk(10, transactionManager)
                    .reader(reader)
                    .processor(processor)
                    .writer(writer)
                    .build();
        } catch (Exception e) {
            System.out.println("Error " + e.getMessage());
        }
        return step;
    }

    @Bean
    public Properties jpaProperties() {
        Properties properties = new Properties();
        try {
            properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        } catch (Exception e) {
            System.out.println("Error " + e.getMessage());
        }
        return properties;
    }
}
