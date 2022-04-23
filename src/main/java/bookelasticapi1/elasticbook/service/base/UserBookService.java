package bookelasticapi1.elasticbook.service.base;

import java.util.List;
import java.util.Set;
import bookelasticapi1.elasticbook.model.sql.Book;

public interface UserBookService {
    Set<Book> getBooksForUser(final String username);
    Book addBookToUser(String username, String bookId);
    Book removeBookFromUser(final String username, final String bookId);
    boolean userOwnsBook(String username, String bookId);
    List<Book> getBooksOwnersAlsoLike(String bookId);
}
