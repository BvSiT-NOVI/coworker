package nl.bvsit.coworker.repository;

import nl.bvsit.coworker.domain.ERole;
import nl.bvsit.coworker.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name);
    boolean existsByName(ERole name);
}
