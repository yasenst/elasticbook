package bookelasticapi1.elasticbook.service.elastic;

import java.io.IOException;
import java.util.List;
import bookelasticapi1.elasticbook.dto.UserDto;
import bookelasticapi1.elasticbook.model.elastic.User;
import bookelasticapi1.elasticbook.model.sql.Book;

public interface ElasticsearchUserService {
    User addBook(Long userId, String bookTitle);
    User removeBook(Long userId, String bookTitle);
    //List<Book> getBooksOwnersAlsoLike(String bookTitle) throws IOException;
    User save(bookelasticapi1.elasticbook.model.sql.User sqlUser);
}
