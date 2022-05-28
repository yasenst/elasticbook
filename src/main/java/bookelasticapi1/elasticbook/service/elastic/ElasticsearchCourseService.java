package bookelasticapi1.elasticbook.service.elastic;

import java.io.IOException;
import java.util.List;
import bookelasticapi1.elasticbook.dto.CourseDto;
import bookelasticapi1.elasticbook.model.elastic.Book;
import bookelasticapi1.elasticbook.model.elastic.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ElasticsearchCourseService {
    Course findById(String courseId);
    Page<Course> findAll(Pageable pageable);
    List<Course> getSampleCourses() throws IOException;
    List<Book> getRecommendedBooksForCourse(String courseId) throws IOException;
    List<Course> multiMatchSearchQuery(String text) throws IOException;

    Course save(final CourseDto courseDto);
    Course delete(final String courseId);
}
