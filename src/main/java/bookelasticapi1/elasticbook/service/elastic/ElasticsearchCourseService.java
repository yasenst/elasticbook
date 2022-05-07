package bookelasticapi1.elasticbook.service.elastic;

import java.io.IOException;
import java.util.List;
import bookelasticapi1.elasticbook.dto.CourseDto;
import bookelasticapi1.elasticbook.model.elastic.Book;
import bookelasticapi1.elasticbook.model.elastic.Course;

public interface ElasticsearchCourseService {
    Course findById(String courseId);
    List<Course> getSampleCourses() throws IOException;
    List<Book> getRecommendedBooksForCourse(String courseId) throws IOException;
    List<Course> multiMatchSearchQuery(String text);

    Course save(final CourseDto courseDto);
    Course delete(final String courseId);

    void indexData();
}
