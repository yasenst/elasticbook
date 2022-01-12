package bookelasticapi1.elasticbook.repository.elastic;

import bookelasticapi1.elasticbook.model.elastic.Course;
import bookelasticapi1.elasticbook.model.elastic.UserOwnedBooks;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ElasticUserOwnedBooksRepository extends ElasticsearchRepository<UserOwnedBooks, String> {
    UserOwnedBooks findByUserId(final String userId);
}
