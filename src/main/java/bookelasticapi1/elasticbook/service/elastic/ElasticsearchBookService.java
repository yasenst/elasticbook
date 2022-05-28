package bookelasticapi1.elasticbook.service.elastic;

import java.io.IOException;
import java.util.List;
import bookelasticapi1.elasticbook.dto.BookDto;
import bookelasticapi1.elasticbook.model.elastic.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ElasticsearchBookService {
    Book findById(String bookId);
    Page<Book> findAll(Pageable pageable);
    Page<Book> findByAuthor(String author, Pageable pageable);
    Page<Book> findByTitle(String title, Pageable pageable);
    Page<Book> findBySubject(String subject, Pageable pageable);

    String[] getSubjects();

    List<Book> getSampleBooks() throws IOException;
    List<Book> moreLikeThis(String bookId) throws IOException;
    List<Book> getRecommendationsList(List<String> bookIdList) throws IOException;
    List<Book> getBooksOwnersAlsoLike(String bookTitle) throws IOException;

    List<Book> multiMatchSearchQuery(String text) throws IOException;

    Book save(BookDto bookDto);
    Book update(String bookId, BookDto bookDto);
    Book delete(String bookId);
}
