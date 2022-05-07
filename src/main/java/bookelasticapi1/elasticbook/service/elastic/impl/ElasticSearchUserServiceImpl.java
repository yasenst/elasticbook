package bookelasticapi1.elasticbook.service.elastic.impl;

import bookelasticapi1.elasticbook.model.elastic.User;
import bookelasticapi1.elasticbook.repository.elastic.ElasticsearchUserRepository;
import bookelasticapi1.elasticbook.service.elastic.ElasticsearchUserService;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ElasticSearchUserServiceImpl implements ElasticsearchUserService {

    private final ElasticsearchUserRepository userRepository;
    private final RestHighLevelClient client;

    @Autowired
    public ElasticSearchUserServiceImpl(final ElasticsearchUserRepository userRepository, final RestHighLevelClient client) {
        this.userRepository = userRepository;
        this.client = client;
    }

    @Override
    public User addBook(String username, String bookTitle) {
        final User document = userRepository.findByUsername(username);
        document.addBook(bookTitle);
        return userRepository.save(document);
    }

    @Override
    public User removeBook(String username, String bookTitle) {
        final User document = userRepository.findByUsername(username);
        document.removeBook(bookTitle);
        return userRepository.save(document);
    }

    @Override
    public User save(bookelasticapi1.elasticbook.model.sql.User sqlUser) {
        User newUser = new User(sqlUser.getId(), sqlUser.getUsername());
        return userRepository.save(newUser);
    }
}
