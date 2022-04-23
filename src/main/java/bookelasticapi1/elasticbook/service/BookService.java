package bookelasticapi1.elasticbook.service;


import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import bookelasticapi1.elasticbook.exception.ElkException;
import bookelasticapi1.elasticbook.dto.BookDto;
import bookelasticapi1.elasticbook.model.Subject;
import bookelasticapi1.elasticbook.model.elastic.Book;
import bookelasticapi1.elasticbook.repository.elastic.ElasticsearchBookRepository;
import bookelasticapi1.elasticbook.repository.sql.SqlBookRepository;
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
import org.springframework.data.domain.Page;
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
public class BookService {

    @NonNull
    private final ElasticsearchBookRepository esBookRepository;

    @NonNull
    private final SqlBookRepository sqlBookRepository;

    @Autowired
    private final ElasticsearchOperations elasticsearchTemplate;

    @Autowired
    private final RestHighLevelClient client;

    public Book findById(String bookId) {
        return esBookRepository.findById(bookId)
                .orElseThrow(() -> new ElkException("Book with ID=" + bookId + " was not found!"));
    }

    public Iterable<Book> findAll() {
        return esBookRepository.findAll();
    }

    public Page<Book> findByAuthor(String author, Pageable pageable) {
        return esBookRepository.findByAuthor(author, pageable);
    }

    public Page<Book> findByTitle(String title, Pageable pageable) {
        return esBookRepository.findByTitle(title, pageable);
    }

    public Page<Book> findBySubject(String subject, Pageable pageable) {
        return esBookRepository.findBySubject(subject, pageable);
    }

    public String[] getSubjects() {
        return Arrays.stream(Subject.values()).map(e -> e.value).toArray(String[]::new);
    }

    /** Returns 6 random books. */
    public List<Book> getSampleBooks() throws IOException {
        final String jsonQuery = "{\"function_score\": {\"boost\": \"5\",\"random_score\": {}, \"boost_mode\": \"multiply\"}}";
        final QueryBuilder queryBuilder = QueryBuilders.wrapperQuery(jsonQuery);

        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder).size(6);

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

    /** Returns 6 books, but they are the same with each request. */
    public List<SearchHit<Book>> matchAllQuery() {
        NativeSearchQuery matchAllQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchAllQuery())
                .withMaxResults(6)
                .build();

        return elasticsearchTemplate.search(matchAllQuery, Book.class).getSearchHits();
    }

    /** Return at most 6 'more like this' books */
    public List<Book> moreLikeThis(String bookId) throws IOException {
        final Book book = findById(bookId);
        final String jsonQuery = "{\"more_like_this\":{\"fields\":[\"title\",\"description\"],\"like\":[{\"_id\":\"" +
                                            bookId + "\"}],\"min_term_freq\":1,\"max_query_terms\":2}}";
        final QueryBuilder queryBuilder = QueryBuilders.wrapperQuery(jsonQuery);

        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.boolQuery()
                .must(QueryBuilders.matchQuery("subject", book.getSubject()))
                .must(queryBuilder)).size(6);

        final SearchRequest searchRequest = new SearchRequest(Book.INDEX);
        searchRequest.source(searchSourceBuilder);

        final SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

        org.elasticsearch.search.SearchHit[] searchHits = response.getHits().getHits();
        return Arrays.stream(searchHits)
                .map(hit -> {
                    final Book bookObj = JSON.parseObject(hit.getSourceAsString(), Book.class);
                    bookObj.setId(hit.getId());
                    return bookObj;
                })
                .collect(Collectors.toList());
    }

    public List<SearchHit<Book>> moreLikeThisOld(String bookId) {
        final Book book = findById(bookId);

        final MoreLikeThisQueryBuilder queryBuilder = QueryBuilders
                .moreLikeThisQuery(new String[] { "title", "description" },
                                    new String[] { book.getTitle(), book.getDescription() },
                                    new MoreLikeThisQueryBuilder.Item[]{new MoreLikeThisQueryBuilder.Item(Book.INDEX, bookId)})
                .minTermFreq(1)
                .maxQueryTerms(3);

        final NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.matchQuery("subject", book.getSubject()))
                        .must(queryBuilder))
                .withPageable(Pageable.unpaged())
                .build();

        SearchHits<Book> searchHits = elasticsearchTemplate.search(query, Book.class);
        return searchHits.stream()
                .filter(searchHit -> (searchHit.getScore() > searchHits.getMaxScore()*0.5))
                .collect(Collectors.toList());
    }

    /** Performs more_like_this query on the bookIdList and returns a list of similar books. */
    public List<Book> getRecommendationsList(final List<String> bookIdList) throws IOException {
        MoreLikeThisQueryBuilder.Item[] mltQueryItems = new MoreLikeThisQueryBuilder.Item[bookIdList.size()];
        int mltQueryItemIndex = 0;
        for (String bookId : bookIdList) {
            mltQueryItems[mltQueryItemIndex++] =  new MoreLikeThisQueryBuilder.Item(Book.INDEX, bookId);
        }

        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder
                .query(QueryBuilders
                        .moreLikeThisQuery(mltQueryItems)
                        .minTermFreq(1)
                        .maxQueryTerms(3))
                .size(6);

        final SearchRequest searchRequest = new SearchRequest(Book.INDEX);
        searchRequest.source(searchSourceBuilder);

        final SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

        return Arrays.stream(response.getHits().getHits())
                .map(hit -> {
                    final Book bookObj = JSON.parseObject(hit.getSourceAsString(), Book.class);
                    bookObj.setId(hit.getId());
                    return bookObj;
                })
                .collect(Collectors.toList());
    }

    /** Performs elasticsearch multi_match_query on the books index. */
    public List<Book> multiMatchSearchQuery(String text) {
        final NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(text, "title", "description", "author")
                        .type(MultiMatchQueryBuilder.Type.PHRASE_PREFIX))
                .withPageable(Pageable.unpaged())
                .build();

        SearchHits<Book> searchHits = elasticsearchTemplate.search(query, Book.class);
        return searchHits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }

    //@PostConstruct
    public void populateMySQL() {
        for (Book elasticBook : esBookRepository.findAll()) {
            bookelasticapi1.elasticbook.model.sql.Book sqlBook = new bookelasticapi1.elasticbook.model.sql.Book();
            sqlBook.setId(elasticBook.getId());
            sqlBook.setAuthor(elasticBook.getAuthor());
            sqlBook.setTitle(elasticBook.getTitle());
            sqlBook.setDescription(elasticBook.getDescription());
            sqlBook.setSubject(elasticBook.getSubject());
            sqlBookRepository.save(sqlBook);
        }
    }

    public Book save(BookDto bookDto) {
        final Book newBook = new Book();
        newBook.setTitle(bookDto.getTitle());
        newBook.setDescription(bookDto.getDescription());
        newBook.setAuthor(bookDto.getAuthor());
        newBook.setSubject(bookDto.getSubject());

        final Book savedBook = esBookRepository.save(newBook);

        bookelasticapi1.elasticbook.model.sql.Book sqlBook = new bookelasticapi1.elasticbook.model.sql.Book();
        sqlBook.setId(savedBook.getId());
        sqlBook.setAuthor(savedBook.getAuthor());
        sqlBook.setTitle(savedBook.getTitle());
        sqlBook.setDescription(savedBook.getDescription());
        sqlBook.setSubject(savedBook.getSubject());
        sqlBookRepository.save(sqlBook);

        return savedBook;
    }

    /** Delete book by id - deletes both in sql and es server as the book has the same id everywhere. */
    public Book delete(final String bookId) {
        final Book esBook = findById(bookId);

        esBookRepository.deleteById(bookId);
        sqlBookRepository.deleteById(bookId);

        return esBook;
        /*final Book esBook = findById(bookId);
        if (esBook != null) {
            esBookRepository.deleteById(bookId);
        } else {
            return false;
        }
        sqlBookRepository.deleteById(bookId);
        try {
            final bookelasticapi1.elasticbook.model.sql.Book sqlBook = sqlBookRepository.getById(bookId);
            sqlBookRepository.deleteById(bookId);
            return true;
        } catch (Exception e) {
            return false;
        }*/
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

    public void indexData() {
        final CsvFileParser<Book> bookCsvParser = new CsvFileParser<Book>(Book.class);
        List<Book> csvData = bookCsvParser.parse("src/main/resources/static/books.csv");
        esBookRepository.saveAll(csvData);
    }

}
