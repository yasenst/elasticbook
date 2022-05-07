package bookelasticapi1.elasticbook.repository.elastic;

import java.util.List;
import bookelasticapi1.elasticbook.model.elastic.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElasticsearchBookRepository extends ElasticsearchRepository<Book, String> {
    Page<Book> findByAuthor(String author, Pageable pageable);
    Page<Book> findByTitle(String title, Pageable pageable);
    Page<Book> findBySubject(String subject, Pageable pageable);
    List<Book> getBooksByTitleIsIn(List<String> titles);

    @Query("{\n" +
            "\"more_like_this\": {\n" +
            "\t\"fields\": [\n" +
            "\t\t\"description\"\n" +
            "\t],\n" +
            "\t\"like\": [\n" +
            "\t\t{\n" +
            "\t\t\t\"_index\": \"book\",\n" +
            "\t\t\t\"_id\": \"?0\"\n" +
            "\t\t}\n" +
            "\t],\n" +
            "\t\"min_term_freq\": 1,\n" +
            "\t\"max_query_terms\": 12\n" +
            "}\n" +
            "}")
    Page<Book> simpleMoreLikeThis(String bookId, Pageable pageable);


    @Query("{\n" +
            "    \"more_like_this\": {\n" +
            "      \"like\": [\n" +
            "        {\n" +
            "          \"_id\": \"?0\"\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  }")
    Page<Book> moreLikeThisTest(String bookId, Pageable pageable);
}
