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
@RequestMapping("/api/es/books")
@Slf4j
public class BookController {

    private final BookService bookService;

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/{id}")
    public Book getById(@PathVariable String id) {
        final Book book = bookService.findById(id);
        return book;
    }

    @GetMapping("/subject/{subject}")
    public Page<Book> getBySubject(@PathVariable String subject) {
        return bookService.findBySubject(subject, Pageable.ofSize(6));
    }

    /*@GetMapping("/sample")
    public List<SearchHit<Book>> getSampleBooks() {
        return bookService.matchAllQuery();
    }*/

    @GetMapping("/sample")
    public List<Book> getSampleBooks() {
        try {
            return bookService.getSampleBooks();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return Collections.emptyList();
    }

    @GetMapping("/{id}/morelikethis")
    public List<Book> getMoreLikeThis(@PathVariable String id) {
        try {
            return bookService.moreLikeThis(id);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return Collections.emptyList();
    }

    @GetMapping("/search")
    public List<Book> searchQuery(@RequestParam String text) {
        return bookService.multiMatchSearchQuery(text);
    }

    @PostMapping("/recommendedlist")
    public List<Book> getRecommendationsBaseOnBookList(@RequestBody final List<String> bookIdList) {
        try {
            return bookService.getRecommendationsBaseOnBookList(bookIdList);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return Collections.emptyList();
    }
}
