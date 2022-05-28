package bookelasticapi1.elasticbook.repository.sql;

import java.util.List;
import java.util.Optional;
import bookelasticapi1.elasticbook.model.sql.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByEsBookId(String esBookId);
}