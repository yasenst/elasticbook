package bookelasticapi1.elasticbook.model.elastic;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = Course.INDEX)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    private static final long serialVersionUID = 1L;

    public static final String INDEX = "course";

    @Id
    private String id;

    @JsonProperty("title")
    @Field(type = FieldType.Text, fielddata = true)
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonProperty("subject")
    private String subject;

}
