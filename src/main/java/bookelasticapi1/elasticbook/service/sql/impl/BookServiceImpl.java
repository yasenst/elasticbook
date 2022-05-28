package bookelasticapi1.elasticbook.service.sql.impl;

import bookelasticapi1.elasticbook.dto.BookDto;
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
        return bookRepository.findByEsBookId(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book with ID=" + bookId + " was not found in the database!"));
    }

    @Override
    public Book save(bookelasticapi1.elasticbook.model.elastic.Book esBook) {
        Book sqlBook = new Book();

        sqlBook.setEsBookId(esBook.getId());
        sqlBook.setAuthor(esBook.getAuthor());
        sqlBook.setTitle(esBook.getTitle());
        sqlBook.setDescription(esBook.getDescription());
        sqlBook.setSubject(esBook.getSubject());

        return bookRepository.save(sqlBook);
    }

    @Override
    public Book update(String bookId, BookDto bookDto) {
        final Book bookToUpdate = getById(bookId);

        bookToUpdate.setAuthor(bookDto.getAuthor());
        bookToUpdate.setTitle(bookDto.getTitle());
        bookToUpdate.setDescription(bookDto.getDescription());
        bookToUpdate.setSubject(bookDto.getSubject());

        return bookRepository.save(bookToUpdate);
    }

    @Override
    public void deleteById(String bookId) {
        Book book = getById(bookId);
        bookRepository.deleteById(book.getId());
    }
}
