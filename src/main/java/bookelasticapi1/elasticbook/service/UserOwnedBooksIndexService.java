package bookelasticapi1.elasticbook.service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import bookelasticapi1.elasticbook.model.elastic.UserOwnedBooks;
import bookelasticapi1.elasticbook.model.sql.Book;
import bookelasticapi1.elasticbook.model.sql.User;
import bookelasticapi1.elasticbook.repository.elastic.ElasticUserOwnedBooksRepository;
import bookelasticapi1.elasticbook.repository.sql.SqlBookRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.elasticsearch.search.aggregations.bucket.global.Global;
import org.elasticsearch.search.aggregations.bucket.filter.Filters;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserOwnedBooksIndexService {

    private static final char SPECIAL_SYMBOL = '-';
    private static final char ENCODER_SYMBOL = '_';

    @NonNull
    private final ElasticUserOwnedBooksRepository esUserOwnedBooksRepository;

    @NonNull
    private final SqlBookRepository sqlBookRepository;

    @Autowired
    private final RestHighLevelClient client;

    public void addBook(final String userId, final String bookId) {
        UserOwnedBooks row = esUserOwnedBooksRepository.findByUserId(userId);
        row.addBook(bookId.replace(SPECIAL_SYMBOL, ENCODER_SYMBOL));
        esUserOwnedBooksRepository.save(row);
    }

    public void removeBook(final String userId, final String bookId) {
        UserOwnedBooks row = esUserOwnedBooksRepository.findByUserId(userId);
        row.removeBook(bookId.replace(SPECIAL_SYMBOL, ENCODER_SYMBOL));
        esUserOwnedBooksRepository.save(row);
    }

    public List<Book> getBooksOwnersAlsoLike(String bookId) throws IOException {
        String encodedBookId = bookId.replace(SPECIAL_SYMBOL, ENCODER_SYMBOL);
        String queryString = "{\"match\": {\"ownedBooks\": \"" + encodedBookId +  "\"}}";
        final QueryBuilder queryBuilder = QueryBuilders.wrapperQuery(queryString);

        final String aggregationName = "user_who_like_this_also_like";
        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder
                .query(queryBuilder)
                .aggregation(AggregationBuilders
                        .terms(aggregationName)
                        .field("ownedBooks")
                        .minDocCount(1)
                        .size(4));

        final SearchRequest searchRequest = new SearchRequest(UserOwnedBooks.INDEX);
        searchRequest.source(searchSourceBuilder);

        final SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

        ParsedStringTerms aggregation = response.getAggregations().get(aggregationName);
        List<String> keys = aggregation.getBuckets().stream()
                .map(bucket -> bucket.getKeyAsString().replace(ENCODER_SYMBOL, SPECIAL_SYMBOL))
                .collect(Collectors.toList());
        keys.remove(bookId.toLowerCase()); // remove the book itself as it  is part of the aggregation

        return sqlBookRepository.findByIdIgnoreCase(keys);
    }

    public void save(final User user) {
        UserOwnedBooks newEsUser = new UserOwnedBooks(user.getId(), user.getUsername());
        esUserOwnedBooksRepository.save(newEsUser);
    }
}
