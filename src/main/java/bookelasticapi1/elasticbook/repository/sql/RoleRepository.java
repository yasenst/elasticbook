package bookelasticapi1.elasticbook.repository.sql;

import bookelasticapi1.elasticbook.model.sql.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Role findRoleByName(String name);
}
