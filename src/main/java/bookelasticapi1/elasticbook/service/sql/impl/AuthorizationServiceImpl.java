package bookelasticapi1.elasticbook.service.sql.impl;

import bookelasticapi1.elasticbook.model.sql.User;
import bookelasticapi1.elasticbook.repository.sql.UserRepository;
import bookelasticapi1.elasticbook.service.sql.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service(value = "authService")
public class AuthorizationServiceImpl implements AuthorizationService {

    private final UserRepository userRepository;

    @Autowired
    public AuthorizationServiceImpl(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean hasAccess(Authentication authentication, long userId) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username);

        return user.getId() == userId;
    }
}
