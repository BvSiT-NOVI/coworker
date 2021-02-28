package nl.bvsit.coworker.repository;

import nl.bvsit.coworker.domain.ERole;
import nl.bvsit.coworker.domain.Role;
import nl.bvsit.coworker.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("select user from User  user " +
            "left join fetch user.cwSessions sessions " +
            "left join fetch user.cwSessionOrders orders " +
            "where user.username =:username")
    Optional<User> findByNameFetchAll(@Param("username") String username);

    @Query("select case when count(u)> 0 then true else false end "+
            "from User u left join u.roles r where r.name=:role")
    boolean existsByRole(ERole role);

    @Query("select u from User u join fetch u.roles r where r.name =:role")
    List<User> findByRole(ERole role);

    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    Optional<User> findFirstByRolesContains(Role role);
    boolean existsByRolesContains(Role role);
    //Optional<User> findByRole



}
