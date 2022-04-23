package bookelasticapi1.elasticbook.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class BookDto {
    private String title;
    private String description;
    private String author;
    private String subject;
}
