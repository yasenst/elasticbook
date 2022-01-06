package bookelasticapi1.elasticbook.service;

import java.util.Set;
import bookelasticapi1.elasticbook.model.sql.Book;

public interface UserBookService {
    Set<Book> getBooksForUser(final String username);
    Book addBookToUser(String username, String bookId);
    boolean userOwnsBook(String username, String bookId);
}
