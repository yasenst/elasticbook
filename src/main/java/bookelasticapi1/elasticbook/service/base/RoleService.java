package bookelasticapi1.elasticbook.service.base;

import bookelasticapi1.elasticbook.model.sql.Role;

public interface RoleService {
    Role findByName(String name);
}
