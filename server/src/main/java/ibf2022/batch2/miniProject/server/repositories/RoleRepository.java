package ibf2022.batch2.miniProject.server.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ibf2022.batch2.miniProject.server.model.ERole;
import ibf2022.batch2.miniProject.server.model.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
  Optional<Role> findByName(ERole name);
}
