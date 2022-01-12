package bookelasticapi1.elasticbook.service;

import java.util.List;
import java.util.Set;
import bookelasticapi1.elasticbook.model.sql.Book;

public interface UserBookService {
    Set<Book> getBooksForUser(final String username);
    Book addBookToUser(String username, String bookId);
    void removeBookFromUser(final String username, final String bookId);
    boolean userOwnsBook(String username, String bookId);
    List<Book> getBooksOwnersAlsoLike(String bookId);
}
