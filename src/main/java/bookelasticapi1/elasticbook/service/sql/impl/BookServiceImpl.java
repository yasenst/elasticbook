package bookelasticapi1.elasticbook.service.sql.impl;

import bookelasticapi1.elasticbook.exception.EntityNotFoundException;
import bookelasticapi1.elasticbook.model.sql.Book;
import bookelasticapi1.elasticbook.repository.sql.BookRepository;
import bookelasticapi1.elasticbook.service.sql.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    @Autowired
    public BookServiceImpl(final BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public Book getById(String bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book with ID=" + bookId + " was not found in the database!"));
    }

    @Override
    public Book save(bookelasticapi1.elasticbook.model.elastic.Book esBook) {
        Book sqlBook = new Book();
        sqlBook.setId(esBook.getId());
        sqlBook.setAuthor(esBook.getAuthor());
        sqlBook.setTitle(esBook.getTitle());
        sqlBook.setDescription(esBook.getDescription());
        sqlBook.setSubject(esBook.getSubject());

        return bookRepository.save(sqlBook);
    }

    @Override
    public void deleteById(String bookId) {
        bookRepository.deleteById(bookId);
    }
}
