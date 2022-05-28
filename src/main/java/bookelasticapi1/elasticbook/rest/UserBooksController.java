package bookelasticapi1.elasticbook.rest;

import java.util.Set;

import bookelasticapi1.elasticbook.exception.EntityNotFoundException;
import bookelasticapi1.elasticbook.model.sql.Book;
import bookelasticapi1.elasticbook.model.sql.User;
import bookelasticapi1.elasticbook.service.elastic.ElasticsearchUserService;

import bookelasticapi1.elasticbook.service.sql.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserBooksController {

    private final ElasticsearchUserService elasticsearchUserService;

    private final UserService userService;

    @Autowired
    public UserBooksController(final ElasticsearchUserService elasticsearchUserService,
                               final UserService userService) {
        this.elasticsearchUserService = elasticsearchUserService;
        this.userService = userService;
    }

    @PreAuthorize("hasRole('ADMIN') or @authService.hasAccess(authentication, #userId)")
    @GetMapping("/{userId}/books")
    public ResponseEntity<Set<Book>> getBooksForUser(@PathVariable long userId) {
        Set<Book> books = userService.getBooks(userId);
        return new ResponseEntity<>(books, HttpStatus.OK);
    }


    @PreAuthorize("isAuthenticated() and @authService.hasAccess(authentication, #userId)")
    @PostMapping("/{userId}/books/{bookId}")
    public ResponseEntity<Book> addBookToUser(@PathVariable long userId, @PathVariable String bookId) {
        try {
            final Book book = userService.addBook(userId, bookId);
            elasticsearchUserService.addBook(userId, book.getTitle());

            return new ResponseEntity<>(book, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PreAuthorize("isAuthenticated() and @authService.hasAccess(authentication, #userId)")
    @DeleteMapping("/{userId}/books/{bookId}")
    public ResponseEntity<Book> removeBookFromUser(@PathVariable long userId, @PathVariable String bookId) {
        try {
            final Book book = userService.removeBook(userId, bookId);
            elasticsearchUserService.removeBook(userId, book.getTitle());

            return new ResponseEntity<>(book, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PreAuthorize("hasRole('ADMIN') or @authService.hasAccess(authentication, #userId)")
    @GetMapping("/{userId}/books/{bookId}/ownership")
    public boolean userOwnsBook(@PathVariable long userId, @PathVariable String bookId) {
        return userService.hasBook(userId, bookId);
    }
}
