package bookelasticapi1.elasticbook.repository.elastic;

import bookelasticapi1.elasticbook.model.elastic.Course;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElasticsearchCourseRepository extends ElasticsearchRepository<Course, String> {
}
