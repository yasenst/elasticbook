package bookelasticapi1.elasticbook.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {
    private String username;
    private String password;
    private String firstName;
    private String lastName;
}