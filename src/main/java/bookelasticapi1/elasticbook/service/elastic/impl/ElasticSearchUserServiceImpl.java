package bookelasticapi1.elasticbook.service.elastic.impl;

import bookelasticapi1.elasticbook.model.elastic.User;
import bookelasticapi1.elasticbook.repository.elastic.ElasticsearchUserRepository;
import bookelasticapi1.elasticbook.service.elastic.ElasticsearchUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ElasticSearchUserServiceImpl implements ElasticsearchUserService {

    private final ElasticsearchUserRepository userRepository;

    @Autowired
    public ElasticSearchUserServiceImpl(final ElasticsearchUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User addBook(Long userId, String bookTitle) {
        final User document = userRepository.findByUserId(userId);
        document.addBook(bookTitle);
        return userRepository.save(document);
    }

    @Override
    public User removeBook(Long userId, String bookTitle) {
        final User document = userRepository.findByUserId(userId);
        document.removeBook(bookTitle);
        return userRepository.save(document);
    }

    @Override
    public User save(bookelasticapi1.elasticbook.model.sql.User sqlUser) {
        User newUser = new User(sqlUser.getId(), sqlUser.getUsername());
        return userRepository.save(newUser);
    }
}
