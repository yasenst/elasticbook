package bookelasticapi1.elasticbook.service.sql;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import bookelasticapi1.elasticbook.dto.UserDto;
import bookelasticapi1.elasticbook.model.sql.Book;
import bookelasticapi1.elasticbook.model.sql.User;

public interface UserService {
    User save(UserDto userDto);
    User save(User user);
    List<User> findAll();
    User findByUsername(String username);
    boolean existsByUsername(String username);

    Set<Book> getBooks(String username);
    Book addBook(String username,String bookId);
    Book removeBook(String username,String bookId);
    boolean hasBook(String username, String bookId);
}
