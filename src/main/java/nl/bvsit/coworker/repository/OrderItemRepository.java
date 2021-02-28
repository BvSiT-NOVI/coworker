package nl.bvsit.coworker.repository;

import nl.bvsit.coworker.domain.CpMenuItem;
import nl.bvsit.coworker.domain.CwSessionOrder;
import nl.bvsit.coworker.domain.OrderItem;
import nl.bvsit.coworker.domain.OrderItemKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, OrderItemKey> {
    @Query("select orderItem from OrderItem orderItem " +
            "join fetch orderItem.id.item  " +
            "where orderItem.id =:orderItemKey")
    Optional<OrderItem> findByIdFetchItem(@Param("orderItemKey") OrderItemKey orderItemKey);


    @Query("select orderItem from OrderItem orderItem  " +
            "where orderItem.id.order.id =:id")
    List<OrderItem> findByOrderWhereIdIs(@Param("id") Long id);

    Optional<OrderItem> findFirstByIdItemAndIdOrder(CpMenuItem item,CwSessionOrder order);   //                 CpMenuItemIdAndCwSessionOrderId(Long menuItemId,Long orderId);
    List<OrderItem> findByIdOrder(CwSessionOrder cwSessionOrder);
}
