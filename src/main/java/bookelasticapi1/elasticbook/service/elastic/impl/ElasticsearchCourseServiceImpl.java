package bookelasticapi1.elasticbook.service.elastic.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import bookelasticapi1.elasticbook.exception.ElkException;
import bookelasticapi1.elasticbook.dto.CourseDto;
import bookelasticapi1.elasticbook.model.elastic.Book;
import bookelasticapi1.elasticbook.model.elastic.Course;
import bookelasticapi1.elasticbook.repository.elastic.ElasticsearchCourseRepository;
import bookelasticapi1.elasticbook.service.elastic.ElasticsearchCourseService;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.MoreLikeThisQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ElasticsearchCourseServiceImpl implements ElasticsearchCourseService {

    private final ElasticsearchCourseRepository esCourseRepository;

    private final RestHighLevelClient client;

    @Autowired
    public ElasticsearchCourseServiceImpl(final ElasticsearchCourseRepository esCourseRepository,
                                          final RestHighLevelClient client) {
        this.esCourseRepository = esCourseRepository;
        this.client = client;
    }

    public Course findById(String courseId) {
        return esCourseRepository.findById(courseId)
                .orElseThrow(() -> new ElkException("Course with ID=" + courseId + " was not found!"));
    }

    @Override
    public Page<Course> findAll(Pageable pageable) {
        return esCourseRepository.findAll(pageable);
    }

    /** Returns 3 random courses. */
    public List<Course> getSampleCourses() throws IOException {
        final String jsonQuery = "{\"function_score\": {\"boost\": \"5\",\"random_score\": {}, \"boost_mode\": \"multiply\"}}";
        final QueryBuilder queryBuilder = QueryBuilders.wrapperQuery(jsonQuery);

        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder).size(3);

        final SearchRequest searchRequest = new SearchRequest(Course.INDEX);
        searchRequest.source(searchSourceBuilder);

        final SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

        return Arrays.stream(response.getHits().getHits())
                .map(hit -> {
                    final Course course = JSON.parseObject(hit.getSourceAsString(), Course.class);
                    course.setId(hit.getId());
                    return course;
                })
                .collect(Collectors.toList());
    }

    /** Returns a list of books recommended for course with id=courseId. */
    public List<Book> getRecommendedBooksForCourse(final String courseId) throws IOException {
        final Course course = findById(courseId);

        final MoreLikeThisQueryBuilder queryBuilder = QueryBuilders
                .moreLikeThisQuery(new String[] { "title", "description" },
                        new String[] { course.getTitle(), course.getDescription() },
                        new MoreLikeThisQueryBuilder.Item[]{new MoreLikeThisQueryBuilder.Item(Course.INDEX, courseId)})
                .minTermFreq(1)
                .maxQueryTerms(2);

        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder
                .query(QueryBuilders.boolQuery()
                        .must(QueryBuilders.matchQuery("subject", course.getSubject()))
                        .must(queryBuilder))
                .size(6);

        final SearchRequest searchRequest = new SearchRequest(Book.INDEX);
        searchRequest.source(searchSourceBuilder);

        final SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

        return Arrays.stream(response.getHits().getHits())
                .map(hit -> {
                    final Book book = JSON.parseObject(hit.getSourceAsString(), Book.class);
                    book.setId(hit.getId());
                    return book;
                })
                .collect(Collectors.toList());
    }

    /** Performs elasticsearch multi_match_query on the courses index. */
    public List<Course> multiMatchSearchQuery(final String text) throws IOException {
        final NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(text, "title", "description")
                        .type(MultiMatchQueryBuilder.Type.BEST_FIELDS))
                .withPageable(Pageable.unpaged())
                .build();

        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder
                .query(QueryBuilders.multiMatchQuery(text, "title", "description")
                                .type(MultiMatchQueryBuilder.Type.PHRASE_PREFIX));

        final SearchRequest searchRequest = new SearchRequest(Course.INDEX);
        searchRequest.source(searchSourceBuilder);

        final SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
        org.elasticsearch.search.SearchHit[] searchHits = response.getHits().getHits();

        return Arrays.stream(searchHits)
                .map(hit -> {
                    final Course course = JSON.parseObject(hit.getSourceAsString(), Course.class);
                    course.setId(hit.getId());
                    return course;
                })
                .collect(Collectors.toList());
    }

    public Course save(final CourseDto courseDto) {
        final Course newCourse = new Course();
        newCourse.setTitle(courseDto.getTitle());
        newCourse.setDescription(courseDto.getDescription());
        newCourse.setSubject(courseDto.getSubject());

        return esCourseRepository.save(newCourse);
    }

    public Course delete(final String courseId) {
        final Course course = findById(courseId);
        esCourseRepository.deleteById(courseId);
        return course;
    }
}
