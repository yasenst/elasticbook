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
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Entity
@Table(name = "books")
@Getter
@Setter
public class Book {
    @Id
    @Column(name = "book_id")
    private String id;

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
