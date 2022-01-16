package bookelasticapi1.elasticbook.rest;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import bookelasticapi1.elasticbook.model.elastic.Book;
import bookelasticapi1.elasticbook.service.BookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
@Slf4j
public class BookController {

    private final BookService bookService;

    @Autowired
    public BookController(final BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/{bookId}")
    public Book getById(@PathVariable final String bookId) {
        return bookService.findById(bookId);
    }

    @GetMapping("/subjects/{subject}")
    public Page<Book> getBySubject(@PathVariable final String subject) {
        return bookService.findBySubject(subject, Pageable.ofSize(6));
    }

    @GetMapping("/subjects")
    public String[] getSubjects() {
        return bookService.getSubjects();
    }

    @GetMapping("/sample")
    public List<Book> getSampleBooks() {
        try {
            return bookService.getSampleBooks();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return Collections.emptyList();
    }

    @GetMapping("/{bookId}/morelikethis")
    public List<Book> getMoreLikeThis(@PathVariable final String bookId) {
        try {
            return bookService.moreLikeThis(bookId);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return Collections.emptyList();
    }

    @GetMapping("/search")
    public List<Book> searchBooks(@RequestParam String text) {
        return bookService.multiMatchSearchQuery(text);
    }

    @PostMapping("/recommended")
    public List<Book> getRecommendationsList(@RequestBody final List<String> bookIdList) {
        try {
            return bookService.getRecommendationsList(bookIdList);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return Collections.emptyList();
    }
}
