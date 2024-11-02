package org.example.springbatch;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.core.io.FileSystemResource;

import java.io.File;

public class BookEntityCsvWriter implements ItemWriter<Book> {
    private static final String CSV_FILE = "output.csv";
    private final FlatFileItemWriter<Book> writer;

    public BookEntityCsvWriter() {
        this.writer = new FlatFileItemWriter<>();
        initializeWriter();
    }

    private void initializeWriter() {
        writer.setResource(new FileSystemResource(CSV_FILE));
        writer.setLineAggregator(new DelimitedLineAggregator<Book>() {
            {
                setDelimiter(",");
                setFieldExtractor(new BeanWrapperFieldExtractor<>() {
                    {
                        setNames(new String[]{"id", "author", "name", "price"});
                    }
                });
            }
        });
    }

    @PostConstruct
    public void openWriter() throws Exception {
        writer.open(new ExecutionContext());
    }

    @PreDestroy
    public void closeWriter() throws Exception {
        writer.close();
    }

    @Override
    public void write(Chunk<? extends Book> items) throws Exception {
        writer.write(items);
    }
}
