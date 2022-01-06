package bookelasticapi1.elasticbook.rest;

import java.util.Set;
import bookelasticapi1.elasticbook.model.sql.Book;
import bookelasticapi1.elasticbook.service.UserBookService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/books")
public class UserBookController {

    private final UserBookService userBookService;

    public UserBookController(final UserBookService userBookService) {
        this.userBookService = userBookService;
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("")
    public Set<Book> getBooksForUser(Authentication authentication) {
        String username = authentication.getName();
        return userBookService.getBooksForUser(username);
    }


    @PreAuthorize("hasRole('USER')")
    @PostMapping("/add")
    public Book addBookToUser(Authentication authentication, @RequestBody String bookId) {
        String username = authentication.getName();
        return userBookService.addBookToUser(username, bookId);
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/check-ownership")
    public boolean userOwnsBook(Authentication authentication, @RequestBody String bookId) {
        String username = authentication.getName();
        return userBookService.userOwnsBook(username, bookId);
    }
}
