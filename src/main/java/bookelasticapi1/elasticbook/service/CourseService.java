package bookelasticapi1.elasticbook.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import bookelasticapi1.elasticbook.ElkException;
import bookelasticapi1.elasticbook.model.elastic.Book;
import bookelasticapi1.elasticbook.model.elastic.Course;
import bookelasticapi1.elasticbook.repository.elastic.ElasticsearchCourseRepository;
import bookelasticapi1.elasticbook.util.CsvFileParser;
import com.alibaba.fastjson.JSON;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseService {

    @NonNull
    private final ElasticsearchCourseRepository esCourseRepository;

    @Autowired
    private final ElasticsearchOperations elasticsearchTemplate;

    @Autowired
    private final RestHighLevelClient client;

    public Course findById(String courseId) {
        return esCourseRepository.findById(courseId)
                .orElseThrow(() -> new ElkException("Course with ID=" + courseId + " was not found!"));
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
        org.elasticsearch.search.SearchHit[] searchHits = response.getHits().getHits();

        return Arrays.stream(searchHits)
                .map(hit -> {
                    final Course course = JSON.parseObject(hit.getSourceAsString(), Course.class);
                    course.setId(hit.getId());
                    return course;
                })
                .collect(Collectors.toList());
    }

    public List<Book> getRecommendedBooksForCourse(String courseId) throws IOException {
        final Course course = findById(courseId);

        final MoreLikeThisQueryBuilder queryBuilder = QueryBuilders
                .moreLikeThisQuery(new String[] { "title", "description" },
                        new String[] { course.getTitle(), course.getDescription() },
                        new MoreLikeThisQueryBuilder.Item[]{new MoreLikeThisQueryBuilder.Item(Course.INDEX, courseId)})
                .minTermFreq(1)
                .maxQueryTerms(2);

        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.boolQuery()
                .must(QueryBuilders.matchQuery("subject", course.getSubject()))
                .must(queryBuilder)).size(6);

        final SearchRequest searchRequest = new SearchRequest(Book.INDEX);
        searchRequest.source(searchSourceBuilder);

        final SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
        org.elasticsearch.search.SearchHit[] searchHits = response.getHits().getHits();

        return Arrays.stream(searchHits)
                .map(hit -> {
                    final Book book = JSON.parseObject(hit.getSourceAsString(), Book.class);
                    book.setId(hit.getId());
                    return book;
                })
                .collect(Collectors.toList());
    }

    public List<Course> multiMatchSearchQuery(String text) {
        final NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(text, "title", "description")
                        .type(MultiMatchQueryBuilder.Type.BEST_FIELDS))
                .withPageable(Pageable.unpaged())
                .build();

        SearchHits<Course> searchHits = elasticsearchTemplate.search(query, Course.class);
        return searchHits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }

    public void indexData() {
        final CsvFileParser<Course> courseCsvFileParser = new CsvFileParser<Course>(Course.class);
        List<Course> courseCsvData = courseCsvFileParser.parse("src/main/resources/static/courses.csv");
        esCourseRepository.saveAll(courseCsvData);
    }
}
