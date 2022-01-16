package bookelasticapi1.elasticbook.dto;

import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class AuthUser {
    private String token;
    private String id;
    private String username;
    private List<String> roles;
}
