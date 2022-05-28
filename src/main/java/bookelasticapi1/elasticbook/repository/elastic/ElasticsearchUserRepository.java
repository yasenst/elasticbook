package bookelasticapi1.elasticbook.repository.elastic;

import bookelasticapi1.elasticbook.model.elastic.User;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ElasticsearchUserRepository extends ElasticsearchRepository<User, String> {
    User findByUserId(Long userId);
    User findByUsername(String username);
}
