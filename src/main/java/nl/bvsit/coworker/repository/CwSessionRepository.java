package nl.bvsit.coworker.repository;

import nl.bvsit.coworker.domain.CwSession;
import nl.bvsit.coworker.domain.Seat;
import nl.bvsit.coworker.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CwSessionRepository extends JpaRepository<CwSession,Long> {
    @Query("select session from CwSession  session " +
            "left join fetch session.cwSessionOrders orders " +
            "left join fetch orders.orderItems as items " +
            "where session.id =:id")
    Optional<CwSession> findByIdFetchAll(@Param("id") Long id);

    boolean existsBySeatAndEndTimeIsNullAndClosedIsFalse(Seat seat);
    List<CwSession> findBySeatAndEndTimeIsNullAndClosedIsFalse(Seat seat);
    boolean existsBySeatAndSeatIsNotNullAndEndTimeIsNullAndClosedIsFalse(Seat seat);
    boolean existsByUserAndEndTimeIsNullAndClosedIsFalse(User user);
    List<CwSession> findByUser(User user);

}
