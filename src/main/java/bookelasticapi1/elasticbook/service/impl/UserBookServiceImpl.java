package bookelasticapi1.elasticbook.service.impl;

import java.util.Set;
import bookelasticapi1.elasticbook.model.sql.Book;
import bookelasticapi1.elasticbook.model.sql.User;
import bookelasticapi1.elasticbook.repository.sql.SqlBookRepository;
import bookelasticapi1.elasticbook.service.UserBookService;
import bookelasticapi1.elasticbook.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserBookServiceImpl implements UserBookService {

    private final UserService userService;

    private final SqlBookRepository sqlBookRepository;

    public UserBookServiceImpl(final UserService userService, final SqlBookRepository sqlBookRepository) {
        this.userService = userService;
        this.sqlBookRepository = sqlBookRepository;
    }

    @Override
    public Set<Book> getBooksForUser(final String username) {
        final User user = userService.findByUsername(username);
        return user.getBooks();
    }

    @Override
    public Book addBookToUser(final String username, final String bookId) {
        final User user = userService.findByUsername(username);
        final Book book = sqlBookRepository.getById(bookId);
        user.addBook(book);
        userService.save(user);
        return book;
    }

    @Override
    public boolean userOwnsBook(String username, String bookId) {
        final User user = userService.findByUsername(username);
        final Book book = sqlBookRepository.getById(bookId);

        return user.getBooks().contains(book);
    }
}
