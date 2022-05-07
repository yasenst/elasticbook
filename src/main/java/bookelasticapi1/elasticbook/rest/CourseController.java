package bookelasticapi1.elasticbook.rest;

import java.io.IOException;
import java.util.List;

import bookelasticapi1.elasticbook.dto.CourseDto;
import bookelasticapi1.elasticbook.exception.ElkException;
import bookelasticapi1.elasticbook.model.elastic.Book;
import bookelasticapi1.elasticbook.model.elastic.Course;
import bookelasticapi1.elasticbook.service.elastic.ElasticsearchCourseService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses")
@Slf4j
public class CourseController {

    private final ElasticsearchCourseService elasticsearchCourseService;

    @Autowired
    public CourseController(ElasticsearchCourseService elasticsearchCourseService) {
        this.elasticsearchCourseService = elasticsearchCourseService;
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<Course> getById(@PathVariable String courseId) {
        try {
            final Course course = elasticsearchCourseService.findById(courseId);
            return new ResponseEntity<>(course, HttpStatus.OK);
        } catch (ElkException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("")
    public ResponseEntity<Course> createCourse(@RequestBody final CourseDto courseDto) {
        final Course course = this.elasticsearchCourseService.save(courseDto);
        return new ResponseEntity<>(course, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{courseId}")
    public ResponseEntity<Course> deleteCourse(@PathVariable final String courseId) {
        try {
            final Course course = elasticsearchCourseService.delete(courseId);
            return new ResponseEntity<>(course, HttpStatus.OK);
        } catch (ElkException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/sample")
    public ResponseEntity<List<Course>> getSampleCourses() {
        try {
            final List<Course> courses = elasticsearchCourseService.getSampleCourses();
            return new ResponseEntity<>(courses, HttpStatus.OK);
        } catch (IOException ioe) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{courseId}/recommended")
    public ResponseEntity<List<Book>> getRecommendedBooks(@PathVariable String courseId) {
        try {
            final List<Book> books = elasticsearchCourseService.getRecommendedBooksForCourse(courseId);
            return new ResponseEntity<>(books, HttpStatus.OK);
        } catch (IOException ioe) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Course>> searchCourses(@RequestParam String text) {
        final List<Course> courses = elasticsearchCourseService.multiMatchSearchQuery(text);
        return new ResponseEntity<>(courses, HttpStatus.OK);
    }
}
