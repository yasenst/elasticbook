package bookelasticapi1.elasticbook.rest;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import bookelasticapi1.elasticbook.model.elastic.Book;
import bookelasticapi1.elasticbook.model.elastic.Course;
import bookelasticapi1.elasticbook.service.CourseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/es/courses")
@Slf4j
public class CourseController {

    private final CourseService courseService;

    @Autowired
    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping("/{id}")
    public Course getById(@PathVariable String id) {
        return courseService.findById(id);
    }

    @GetMapping("/sample")
    public List<Course> getSampleCourses() {
        try {
            return courseService.getSampleCourses();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return Collections.emptyList();
    }

    @GetMapping("/{courseId}/recommended")
    public List<Book> getRecommendedBooks(@PathVariable String courseId) {
        try {
            return courseService.getRecommendedBooksForCourse(courseId);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return Collections.emptyList();
    }

    @GetMapping("/search")
    public List<Course> searchQuery(@RequestParam String text) {
        return courseService.multiMatchSearchQuery(text);
    }
}
