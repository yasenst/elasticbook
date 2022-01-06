package bookelasticapi1.elasticbook.service;


import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import bookelasticapi1.elasticbook.ElkException;
import bookelasticapi1.elasticbook.model.elastic.Book;
import bookelasticapi1.elasticbook.repository.elastic.BookRepository;
import bookelasticapi1.elasticbook.repository.sql.SqlBookRepository;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.MoreLikeThisQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;


import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {

    private static final int BULK_SIZE = 500;

    @NonNull
    final BookRepository bookRepository;

    @NonNull
    final SqlBookRepository sqlBookRepository;

    @Autowired
    final ElasticsearchOperations elasticsearchTemplate;

    @Autowired
    final RestHighLevelClient client;

    private final CsvMapper csvMapper = new CsvMapper();

    private final CsvSchema schema = csvMapper
            .typedSchemaFor(Book.class)
            .withHeader()
            .withColumnReordering(true);

    public Book findById(String bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new ElkException("Book with ID=" + bookId + " was not found!"));
    }

    //@PostConstruct
    public void populateMySQL() {
        for (Book elasticBook : bookRepository.findAll()) {
            bookelasticapi1.elasticbook.model.sql.Book sqlBook = new bookelasticapi1.elasticbook.model.sql.Book();
            sqlBook.setId(elasticBook.getId());
            sqlBook.setAuthor(elasticBook.getAuthor());
            sqlBook.setTitle(elasticBook.getTitle());
            sqlBook.setDescription(elasticBook.getDescription());
            sqlBook.setSubject(elasticBook.getSubject());
            sqlBookRepository.save(sqlBook);
        }
    }

    public Book save(Book book) {
        return bookRepository.save(book);
    }

    public void delete(Book book) {
        bookRepository.delete(book);
    }

    public Iterable<Book> findAll() {
        return bookRepository.findAll();
    }

    public Page<Book> findByAuthor(String author, Pageable pageable) {
        return bookRepository.findByAuthor(author, pageable);
    }

    public Page<Book> findByTitle(String title, Pageable pageable) {
        return bookRepository.findByTitle(title, pageable);
    }

    public Page<Book> findBySubject(String subject, Pageable pageable) {
        return bookRepository.findBySubject(subject, pageable);
    }

    public SearchHits<Book> matchAllQuery() {
        NativeSearchQuery matchAllQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchAllQuery())
                .withMaxResults(6)
                .build();
        return elasticsearchTemplate.search(matchAllQuery, Book.class);
    }

    public List<SearchHit<Book>> moreLikeThis(String bookId) {
        final Book book = findById(bookId);

        final MoreLikeThisQueryBuilder queryBuilder = QueryBuilders
                .moreLikeThisQuery(new String[] { "title", "subject", "description" },
                                    new String[] { book.getTitle(), book.getSubject() },
                                    new MoreLikeThisQueryBuilder.Item[]{new MoreLikeThisQueryBuilder.Item(Book.INDEX, bookId)})
                .minTermFreq(1)
                .minDocFreq(1)
                .maxQueryTerms(12);

        final NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
                .withPageable(Pageable.unpaged())
                .build();

        SearchHits<Book> searchHits = elasticsearchTemplate.search(query, Book.class);
        return searchHits.stream()
                .filter(searchHit -> (searchHit.getScore() > searchHits.getMaxScore()*0.5))
                .collect(Collectors.toList());
    }

    public SearchHits<Book> multiMatchSearchQuery(String text, String subject) {
        if (subject != null) {
            return boolQuery(text, subject);
        }
        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(text, "title", "description")
                        .type(MultiMatchQueryBuilder.Type.BEST_FIELDS))
                .withPageable(Pageable.unpaged())
                .build();

        return elasticsearchTemplate.search(query, Book.class);
    }

    public SearchHits<Book> boolQuery(String text, String subject) {
        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.boolQuery()
                                        .must(QueryBuilders.matchQuery("subject", subject))
                                        .should(QueryBuilders.multiMatchQuery(text, "title", "description")))
                .withPageable(Pageable.unpaged())
                .build();

        return elasticsearchTemplate.search(query, Book.class);
    }

    public SearchHits<Book> moreLikeThisTest(String description) {
        MoreLikeThisQueryBuilder queryBuilder = QueryBuilders.moreLikeThisQuery(new String[] {"description"}, new String[] {description}, null);
        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
                .withPageable(Pageable.unpaged())
                .build();
        return elasticsearchTemplate.search(query, Book.class);
    }

    public SearchHits<Book> search(String title, String author, String subject, Pageable pageable) {
        IndexCoordinates index = IndexCoordinates.of(Book.INDEX);

        CriteriaQuery query = buildSearchQuery(title, author, subject);
        query.setPageable(pageable);

        return elasticsearchTemplate.search(query, Book.class, index);
    }

    private CriteriaQuery buildSearchQuery(String title, String author, String subject) {
        Criteria criteria = new Criteria();
        if (nonNull(title)) {
            criteria.and(new Criteria("title").contains(title));
        }
        if (nonNull(author)) {
            criteria.and(new Criteria("author").expression(author));
        }
        if (nonNull(subject)) {
            criteria.and(new Criteria("subject").is(subject));
        }
        return new CriteriaQuery(criteria);
    }

    public void uploadFile(String csvFileName) {
        log.info("loading file {} ...", csvFileName);
        List<Book> csvData = parseFile(csvFileName);
        log.info("{} entries loaded from CSV file", csvData.size());
        storeData(csvData);
        log.info("data loading finish");
    }

    List<Book> parseFile(String csvFileName) {
        try {
            return csvMapper
                    .readerFor(Book.class)
                    .with(schema)
                    .<Book>readValues(new File(csvFileName))
                    .readAll();
        } catch (IOException e) {
            throw new ElkException(e);
        }
    }

    private void storeData(List<Book> cities) {
        final AtomicInteger counter = new AtomicInteger();

        final Collection<List<Book>> chunks = cities.stream()
                .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / BULK_SIZE))
                .values();
        counter.set(0);
        chunks.forEach(ch -> {
            bookRepository.saveAll(ch);
            log.info("bulk of cities stored [{}/{}] ...", counter.getAndIncrement(), chunks.size());
        });
    }

    /*public Page<Book> simpleMoreLikeThis(String bookId) {
        return bookRepository.simpleMoreLikeThis(bookId, Pageable.unpaged());
    }*/

    private void explainApiExample() {
        /*try {


            ExplainRequest request = new ExplainRequest(Book.INDEX, bookId);
            request.query(queryBuilder1);

            ExplainResponse explainResponse = client.explain(request, RequestOptions.DEFAULT);
            Explanation explanation = explainResponse.getExplanation();
            System.out.println();

        } catch (IOException e) {
            System.out.println(e);
        }*/
    }
}
