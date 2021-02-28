package nl.bvsit.coworker.repository;

import nl.bvsit.coworker.domain.CpMenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CpMenuItemRepository extends JpaRepository<CpMenuItem,Long> {
    Optional<CpMenuItem> findByName(String name);
    boolean existsByName(String name);
    Optional<CpMenuItem> findFirstByIdIn(List<Long> cpMenuItemIds);
    Optional<CpMenuItem> findFirstByIdNotIn(List<Long> cpMenuItemIds);
    List<CpMenuItem> findByIdIn(List<Long> cpMenuItemIds);
    boolean existsByIdIn(List<Long> cpMenuItemIds);
}
