package nl.bvsit.coworker.repository;

import nl.bvsit.coworker.domain.CwSession;
import nl.bvsit.coworker.domain.CwSessionOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CwSessionOrderRepository extends JpaRepository<CwSessionOrder,Long> {
}
