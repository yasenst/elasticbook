package bookelasticapi1.elasticbook.model.sql;

import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "courses")
@Getter
@Setter
public class Course {
    @Id
    @Column(name = "course_id")
    private String id;

    @Column
    private String name;

    @Column
    private String description;

    @JsonIgnore
    @ManyToMany(mappedBy = "courses")
    private Set<User> users;
}
