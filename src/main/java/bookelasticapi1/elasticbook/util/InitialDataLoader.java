package bookelasticapi1.elasticbook.util;

import java.util.List;
import javax.annotation.PostConstruct;
import bookelasticapi1.elasticbook.model.elastic.Book;
import bookelasticapi1.elasticbook.model.elastic.Course;
import bookelasticapi1.elasticbook.repository.elastic.ElasticsearchBookRepository;
import bookelasticapi1.elasticbook.repository.elastic.ElasticsearchCourseRepository;
import bookelasticapi1.elasticbook.service.sql.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InitialDataLoader {

    private final BookService sqlBookService;

    private final ElasticsearchBookRepository esBookRepository;

    private final ElasticsearchCourseRepository esCourseRepository;

    @Autowired
    public InitialDataLoader(final BookService sqlBookService,
                             final ElasticsearchBookRepository esBookRepository,
                             final ElasticsearchCourseRepository esCourseRepository) {
        this.sqlBookService = sqlBookService;
        this.esBookRepository = esBookRepository;
        this.esCourseRepository = esCourseRepository;
    }

    public void populateMySQL() {
        for (Book elasticBook : esBookRepository.findAll()) {
            sqlBookService.save(elasticBook);
        }
    }

    public void indexBooks() {
        System.out.println("Indexing");
        final CsvFileParser<Book> bookCsvParser = new CsvFileParser<>(Book.class);
        List<Book> csvData = bookCsvParser.parse("src/main/resources/static/books.csv");
        esBookRepository.saveAll(csvData);
    }

    public void indexCourses() {
        final CsvFileParser<Course> courseCsvFileParser = new CsvFileParser<Course>(Course.class);
        List<Course> courseCsvData = courseCsvFileParser.parse("src/main/resources/static/courses.csv");
        esCourseRepository.saveAll(courseCsvData);
    }
}
