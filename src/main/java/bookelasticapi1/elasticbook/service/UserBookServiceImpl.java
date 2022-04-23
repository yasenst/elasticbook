package bookelasticapi1.elasticbook.service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import bookelasticapi1.elasticbook.model.sql.Book;
import bookelasticapi1.elasticbook.model.sql.User;
import bookelasticapi1.elasticbook.repository.sql.SqlBookRepository;
import bookelasticapi1.elasticbook.service.base.UserBookService;
import bookelasticapi1.elasticbook.service.base.UserService;

import org.springframework.stereotype.Service;

@Service
public class UserBookServiceImpl implements UserBookService {

    private final UserService userService;

    private final UserOwnedBooksIndexService elasticsearchUserBooksService;

    private final SqlBookRepository sqlBookRepository;

    public UserBookServiceImpl(final UserService userService,
                               final UserOwnedBooksIndexService elasticsearchUserBooksService,
                               final SqlBookRepository sqlBookRepository) {
        this.userService = userService;
        this.elasticsearchUserBooksService = elasticsearchUserBooksService;
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

        elasticsearchUserBooksService.addBook(user.getId(), bookId);

        return book;
    }

    @Override
    public Book removeBookFromUser(final String username, final String bookId) {
        final User user = userService.findByUsername(username);
        final Book book = sqlBookRepository.getById(bookId);

        user.removeBook(book);
        userService.save(user);

        elasticsearchUserBooksService.removeBook(user.getId(), bookId);

        return book;
    }

    @Override
    public boolean userOwnsBook(String username, String bookId) {
        final User user = userService.findByUsername(username);
        final Book book = sqlBookRepository.getById(bookId);

        return user.getBooks().contains(book);
    }

    @Override
    public List<Book> getBooksOwnersAlsoLike(String bookId) {
        try {
            return elasticsearchUserBooksService.getBooksOwnersAlsoLike(bookId);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return Collections.emptyList();
    }
}
