package org.example.springbatch;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class BookEntityItemProcessor
        implements ItemProcessor<Book, Book> {

    @Override
    public Book process(Book item) throws Exception
    {
        // Không cần xử lý đặc biệt nếu không muốn thay đổi dữ liệu
        return item;
    }
}