package bookelasticapi1.elasticbook.service.base;

import java.util.List;
import java.util.Optional;
import bookelasticapi1.elasticbook.dto.UserDto;
import bookelasticapi1.elasticbook.model.sql.User;

public interface UserService {
    User save(UserDto userDto);
    User save(User user);
    List<User> findAll();
    User findByUsername(String username);
    boolean existsByUsername(String username);
}
