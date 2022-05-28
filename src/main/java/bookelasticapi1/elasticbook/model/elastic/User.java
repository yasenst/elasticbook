package bookelasticapi1.elasticbook.model.elastic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = User.INDEX)
@Data
@AllArgsConstructor
@Getter
@Setter
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String INDEX = "users";

    public User() {
        booksOwned = new ArrayList<>();
    }

    public User(final Long userId, final String username) {
        this.userId = userId;
        this.username = username;
        booksOwned = new ArrayList<>();
    }

    @Id
    private String id;

    @JsonProperty("userId")
    @Field(name = "user_id")
    private Long userId;

    @JsonProperty("username")
    private String username;

    @JsonProperty("booksOwned")
    @Field(name = "books_owned", type = FieldType.Keyword)
    private List<String> booksOwned;

    public void addBook(String bookTitle) {
        booksOwned.add(bookTitle);
    }

    public void removeBook(String bookTitle) {
        booksOwned.remove(bookTitle);
    }
}
