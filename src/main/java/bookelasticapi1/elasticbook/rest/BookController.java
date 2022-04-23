package bookelasticapi1.elasticbook.rest;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import bookelasticapi1.elasticbook.exception.ElkException;
import bookelasticapi1.elasticbook.dto.BookDto;
import bookelasticapi1.elasticbook.model.elastic.Book;
import bookelasticapi1.elasticbook.service.BookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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
    public ResponseEntity<Book> getById(@PathVariable final String bookId) {
        try {
            final Book book = bookService.findById(bookId);
            return new ResponseEntity<>(book, HttpStatus.OK);
        } catch (ElkException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("")
    public ResponseEntity<Book> createBook(@RequestBody final BookDto bookDto) {
        final Book book = this.bookService.save(bookDto);
        return new ResponseEntity<>(book, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{bookId}")
    public ResponseEntity<Book> deleteBook(@PathVariable final String bookId) {
        try {
            final Book book = this.bookService.delete(bookId);
            return new ResponseEntity<>(book, HttpStatus.OK);
        } catch (ElkException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
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
    public ResponseEntity<List<Book>> getSampleBooks() {
        try {
            final List<Book> books = bookService.getSampleBooks();
            return new ResponseEntity<>(books, HttpStatus.OK);
        } catch (IOException ioe) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{bookId}/more")
    public ResponseEntity<List<Book>> getMoreLikeThis(@PathVariable final String bookId) {
        try {
            final List<Book> books = bookService.moreLikeThis(bookId);
            return new ResponseEntity<>(books, HttpStatus.OK);
        } catch (IOException ioe) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Book>> searchBooks(@RequestParam String text) {
        final List<Book> books = bookService.multiMatchSearchQuery(text);
        return new ResponseEntity<>(books, HttpStatus.OK);
    }

    @PostMapping("/recommended")
    public ResponseEntity<List<Book>> getRecommendationsList(@RequestBody final List<String> bookIdList) {
        try {
            final List<Book> books = bookService.getRecommendationsList(bookIdList);
            return new ResponseEntity<>(books, HttpStatus.OK);
        } catch (IOException ioe) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
