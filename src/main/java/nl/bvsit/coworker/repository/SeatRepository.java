package nl.bvsit.coworker.repository;

import nl.bvsit.coworker.domain.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat,Long> {
    boolean existsByCode(String code);
    Optional<Seat> findByCode(String code);
    List<Seat> findByCwSessionsIsNull();
}
