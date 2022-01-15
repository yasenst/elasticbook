package bookelasticapi1.elasticbook.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import bookelasticapi1.elasticbook.dto.UserDto;
import bookelasticapi1.elasticbook.model.sql.Role;
import bookelasticapi1.elasticbook.model.sql.User;
import bookelasticapi1.elasticbook.repository.sql.UserRepository;
import bookelasticapi1.elasticbook.service.base.RoleService;
import bookelasticapi1.elasticbook.service.base.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

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
