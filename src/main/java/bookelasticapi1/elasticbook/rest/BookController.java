package bookelasticapi1.elasticbook.rest;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.websocket.server.PathParam;
import bookelasticapi1.elasticbook.model.elastic.Book;
import bookelasticapi1.elasticbook.service.BookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


import static org.springframework.http.HttpStatus.CREATED;

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
    public List<Book> moreLikeThis(@PathVariable String id) {
        try {
            return bookService.moreLikeThis1(id);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return Collections.emptyList();
    }

    @GetMapping("/{id}/similar")
    public List<SearchHit<Book>> getSimilarBooks(@PathVariable String id) {
        return bookService.moreLikeThis(id);
    }

    /*@GetMapping("/{id}/similar")
    public Page<Book> getSimilarBooks(@PathVariable String id) {
        return bookService.moreLikeThis(id);
    }*/

    @GetMapping("/search")
    public List<Book> searchQuery(@RequestParam String text) {
        return bookService.multiMatchSearchQuery(text);
    }

    @GetMapping
    public SearchHits<Book> search(@PathParam("title") String title,
                                   @PathParam("author") String author,
                                   @PathParam("subject") String subject) {
        return bookService.search(title, author, subject, Pageable.unpaged());
    }

    /*@GetMapping("/experiment/{id}")
    public SearchHits<Book> experimentMoreLikeThis(@PathVariable String id) {
        Book book = bookService.findById(id);
        return bookService.moreLikeThisTest(book.getDescription());
    }*/

    /*@GetMapping("/{id}/moreLikeThis")
    public Page<Book> simpleMoreLikeThis(@PathVariable String id) {
        return bookService.simpleMoreLikeThis(id);
    }*/
}
