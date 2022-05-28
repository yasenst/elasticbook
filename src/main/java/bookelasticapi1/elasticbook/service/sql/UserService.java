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
    User findById(Long userId);
    boolean existsByUsername(String username);

    Set<Book> getBooks(long userId);
    Book addBook(Long userId,String bookId);
    Book removeBook(Long userId,String bookId);
    boolean hasBook(Long userId, String bookId);
}
