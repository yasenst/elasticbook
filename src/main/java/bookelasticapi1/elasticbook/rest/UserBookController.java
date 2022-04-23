package bookelasticapi1.elasticbook.rest;

import java.util.List;
import java.util.Set;

import bookelasticapi1.elasticbook.model.sql.Book;
import bookelasticapi1.elasticbook.service.base.UserBookService;

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
public class UserBookController {

    private final UserBookService userBookService;

    public UserBookController(final UserBookService userBookService) {
        this.userBookService = userBookService;
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("")
    public ResponseEntity<Set<Book>> getBooksForUser(Authentication authentication) {
        String username = authentication.getName();
        Set<Book> books = userBookService.getBooksForUser(username);
        return new ResponseEntity<>(books, HttpStatus.OK);
    }


    @PreAuthorize("hasRole('USER')")
    @PostMapping("")
    public ResponseEntity<Book> addBookToUser(Authentication authentication, @RequestBody String bookId) {
        String username = authentication.getName();
        final Book book = userBookService.addBookToUser(username, bookId);
        return new ResponseEntity<>(book, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{bookId}")
    public ResponseEntity<Book> removeBookFromUser(Authentication authentication, @PathVariable final String bookId) {
        String username = authentication.getName();
        final Book book = userBookService.removeBookFromUser(username, bookId);
        return new ResponseEntity<>(book, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/ownership")
    public boolean userOwnsBook(Authentication authentication, @RequestBody String bookId) {
        String username = authentication.getName();
        return userBookService.userOwnsBook(username, bookId);
    }

    @PostMapping("/recommended")
    public ResponseEntity<List<Book>> getBooksOwnersAlsoLike(@RequestBody String bookId) {
        final List<Book> books = userBookService.getBooksOwnersAlsoLike(bookId);
        return new ResponseEntity<>(books, HttpStatus.OK);
    }
}
