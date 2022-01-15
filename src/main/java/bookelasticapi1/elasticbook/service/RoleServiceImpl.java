package bookelasticapi1.elasticbook.service;

import bookelasticapi1.elasticbook.model.sql.Role;
import bookelasticapi1.elasticbook.repository.sql.RoleRepository;
import bookelasticapi1.elasticbook.service.base.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service(value = "roleService")
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;

    @Autowired
    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Role findByName(String name) {
        return roleRepository.findRoleByName(name);
    }

}
