package bookelasticapi1.elasticbook.model.sql;

import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "books")
@Getter
@Setter
public class Book {
    @Id
    @Column(name = "book_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "es_book_id", unique = true)
    private String esBookId;

    @Column
    private String title;

    @Column
    private String description;

    @Column
    private String author;

    @Column
    private String subject;

    @JsonIgnore
    @ManyToMany(mappedBy = "books")
    private Set<User> users;
}
