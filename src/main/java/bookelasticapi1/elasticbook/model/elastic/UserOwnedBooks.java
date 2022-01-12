package bookelasticapi1.elasticbook.model.elastic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import bookelasticapi1.elasticbook.model.sql.User;
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

@Document(indexName = UserOwnedBooks.INDEX)
@Data
@AllArgsConstructor
@Getter
@Setter
public class UserOwnedBooks implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String INDEX = "user-owned-books";

    public UserOwnedBooks() {
        ownedBooks = new HashSet<>();
    }

    public UserOwnedBooks(final String userId, final String username) {
        this.userId = userId;
        this.username = username;
        ownedBooks = new HashSet<>();
    }

    @Id
    private String id;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("username")
    private String username;

    @JsonProperty("owned_books")
    @Field(type = FieldType.Text, fielddata = true)
    private Set<String> ownedBooks;

    public void addBook(String bookId) {
        ownedBooks.add(bookId);
    }

    public void removeBook(String bookId) {
        ownedBooks.remove(bookId);
    }
}
