package bookelasticapi1.elasticbook.repository.sql;

import bookelasticapi1.elasticbook.model.sql.Book;
import bookelasticapi1.elasticbook.model.sql.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SqlBookRepository extends JpaRepository<Book, String> {
}