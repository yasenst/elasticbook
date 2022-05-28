package bookelasticapi1.elasticbook.service.sql;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

public interface AuthorizationService {
    boolean hasAccess(Authentication authentication, long userId);
}
