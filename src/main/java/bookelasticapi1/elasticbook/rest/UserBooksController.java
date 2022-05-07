package bookelasticapi1.elasticbook.rest;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import bookelasticapi1.elasticbook.exception.EntityNotFoundException;
import bookelasticapi1.elasticbook.model.sql.Book;
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
@RequestMapping("/api/users/books")
public class UserBooksController {

    private final ElasticsearchUserService elasticsearchUserService;

    private final UserService sqlUserService;

    @Autowired
    public UserBooksController(final ElasticsearchUserService elasticsearchUserService,
                               final UserService sqlUserService) {
        this.elasticsearchUserService = elasticsearchUserService;
        this.sqlUserService = sqlUserService;
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("")
    public ResponseEntity<Set<Book>> getBooksForUser(Authentication authentication) {
        String username = authentication.getName();
        Set<Book> books = sqlUserService.getBooks(username);
        return new ResponseEntity<>(books, HttpStatus.OK);
    }


    @PreAuthorize("hasRole('USER')")
    @PostMapping("")
    public ResponseEntity<Book> addBookToUser(Authentication authentication, @RequestBody String bookId) {
        try {
            String username = authentication.getName();

            final Book book = sqlUserService.addBook(username, bookId);
            elasticsearchUserService.addBook(username, book.getTitle());

            return new ResponseEntity<>(book, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{bookId}")
    public ResponseEntity<Book> removeBookFromUser(Authentication authentication, @PathVariable final String bookId) {
        try {
            String username = authentication.getName();

            final Book book = sqlUserService.removeBook(username, bookId);
            elasticsearchUserService.removeBook(username, book.getTitle());

            return new ResponseEntity<>(book, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/ownership")
    public boolean userOwnsBook(Authentication authentication, @RequestBody String bookId) {
        String username = authentication.getName();
        return sqlUserService.hasBook(username, bookId);
    }
}
