package bookelasticapi1.elasticbook.service.sql;

import bookelasticapi1.elasticbook.dto.BookDto;
import bookelasticapi1.elasticbook.model.sql.Book;

public interface BookService {
    Book getById(String bookId);
    Book save(bookelasticapi1.elasticbook.model.elastic.Book esBook);
    void deleteById(String bookId);
}
