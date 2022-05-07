package bookelasticapi1.elasticbook.service.sql.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import bookelasticapi1.elasticbook.dto.UserDto;
import bookelasticapi1.elasticbook.model.sql.Book;
import bookelasticapi1.elasticbook.model.sql.Role;
import bookelasticapi1.elasticbook.model.sql.User;
import bookelasticapi1.elasticbook.repository.sql.UserRepository;
import bookelasticapi1.elasticbook.service.sql.BookService;
import bookelasticapi1.elasticbook.service.sql.RoleService;
import bookelasticapi1.elasticbook.service.sql.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final RoleService roleService;

    private final UserRepository userRepository;

    private final PasswordEncoder encoder;

    private final BookService bookService;

    @Autowired
    public UserServiceImpl(final RoleService roleService,
                           final UserRepository userRepository,
                           final PasswordEncoder encoder,
                           final BookService bookService) {
        this.roleService = roleService;
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.bookService = bookService;
    }

    @Override
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        userRepository.findAll().iterator().forEachRemaining(list::add);
        return list;
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public Set<Book> getBooks(String username) {
        final User user = findByUsername(username);

        return user.getBooks();
    }

    @Override
    public Book addBook(String username, String bookId) {
        final User user = findByUsername(username);
        final Book book = bookService.getById(bookId);

        user.addBook(book);
        save(user);

        return book;
    }

    @Override
    public Book removeBook(String username, String bookId) {
        final User user = findByUsername(username);
        final Book book = bookService.getById(bookId);

        user.removeBook(book);
        save(user);

        return book;
    }

    @Override
    public boolean hasBook(String username, String bookId) {
        final User user = findByUsername(username);
        final Book book = bookService.getById(bookId);

        return user.hasBook(book);
    }

    @Override
    public User save(UserDto userDto) {
        User newUser = new User();
        newUser.setUsername(userDto.getUsername());
        newUser.setPassword(encoder.encode(userDto.getPassword()));

        Role role = roleService.findByName("USER");
        Set<Role> roleSet = new HashSet<>();
        roleSet.add(role);

        newUser.setRoles(roleSet);
        return userRepository.save(newUser);
    }

    public User save(User user) {
        return userRepository.save(user);
    }
}
