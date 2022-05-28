package bookelasticapi1.elasticbook.service.elastic.impl;


import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import bookelasticapi1.elasticbook.exception.ElkException;
import bookelasticapi1.elasticbook.dto.BookDto;
import bookelasticapi1.elasticbook.model.Subject;
import bookelasticapi1.elasticbook.model.elastic.Book;
import bookelasticapi1.elasticbook.model.elastic.User;
import bookelasticapi1.elasticbook.repository.elastic.ElasticsearchBookRepository;
import bookelasticapi1.elasticbook.service.elastic.ElasticsearchBookService;

import com.alibaba.fastjson.JSON;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.MoreLikeThisQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ElasticsearchBookServiceImpl implements ElasticsearchBookService {

    private final ElasticsearchBookRepository esBookRepository;

    private final RestHighLevelClient client;

    @Autowired
    public ElasticsearchBookServiceImpl(final @NonNull ElasticsearchBookRepository esBookRepository,
                                        final RestHighLevelClient client) {
        this.esBookRepository = esBookRepository;
        this.client = client;
    }

    public Book findById(String bookId) {
        return esBookRepository.findById(bookId)
                .orElseThrow(() -> new ElkException("Book with ID=" + bookId + " was not found in the index!"));
    }

    @Override
    public Page<Book> findAll(Pageable pageable) {
        return esBookRepository.findAll(pageable);
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
        final String jsonQuery = "{\"function_score\": {\"query\": { \"match_all\": {} },\"boost\": \"5\",\"random_score\": {}, \"boost_mode\": \"multiply\"}}";
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
                        .minTermFreq(2)
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

    @Override
    public List<Book> getBooksOwnersAlsoLike(String bookId) throws IOException {
        Book book = findById(bookId);
        String queryString = "{\"match\": {\"books_owned\": \"" + book.getTitle() +  "\"}}";
        final QueryBuilder queryBuilder = QueryBuilders.wrapperQuery(queryString);

        final String aggregationName = "users_who_like_this_also_like";
        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder
                .query(queryBuilder)
                .aggregation(AggregationBuilders
                        .terms(aggregationName)
                        .field("books_owned")
                        .minDocCount(1));

        final SearchRequest searchRequest = new SearchRequest(User.INDEX);
        searchRequest.source(searchSourceBuilder);

        final SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

        ParsedStringTerms aggregation = response.getAggregations().get(aggregationName);

        if (aggregation.getBuckets().size() < 1) {
            return Collections.emptyList();
        }

        aggregation.getBuckets().removeIf(bucket -> bucket.getKeyAsString().equals(book.getTitle()));

        double max = aggregation.getBuckets().stream()
                .mapToDouble(bucket -> bucket.getDocCount())
                .max().orElseThrow(NoSuchElementException::new);

        List<String> keys = aggregation.getBuckets().stream()
                .filter(bucket -> bucket.getDocCount() >= max)
                .map(bucket -> bucket.getKeyAsString())
                .collect(Collectors.toList());

        return esBookRepository.getBooksByTitleIsIn(keys);
    }

    /** Performs elasticsearch multi_match_query on the books index. */
    public List<Book> multiMatchSearchQuery(String text) throws IOException {
        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder
                .query(QueryBuilders.multiMatchQuery(text, "title", "description", "author")
                        .type(MultiMatchQueryBuilder.Type.PHRASE_PREFIX));

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

    public Book save(BookDto bookDto) {
        final Book newBook = new Book();
        newBook.setTitle(bookDto.getTitle());
        newBook.setDescription(bookDto.getDescription());
        newBook.setAuthor(bookDto.getAuthor());
        newBook.setSubject(bookDto.getSubject());

        return esBookRepository.save(newBook);
    }

    @Override
    public Book update(String bookId, BookDto bookDto) {
        final Book bookToUpdate = this.findById(bookId);
        bookToUpdate.setTitle(bookDto.getTitle());
        bookToUpdate.setDescription(bookDto.getDescription());
        bookToUpdate.setAuthor(bookDto.getAuthor());
        bookToUpdate.setSubject(bookDto.getSubject());

        return esBookRepository.save(bookToUpdate);
    }

    public Book delete(final String bookId) {
        final Book esBook = findById(bookId);

        esBookRepository.deleteById(bookId);

        return esBook;
    }
}
