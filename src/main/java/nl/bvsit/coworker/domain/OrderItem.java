package nl.bvsit.coworker.domain;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "cs_order_cp_menu_item")
public class OrderItem {
    @EmbeddedId
    OrderItemKey id= new OrderItemKey();

    @Column(columnDefinition = "integer default 1")
    int quantity=1;

    public OrderItem() {}

    public OrderItem(CwSessionOrder order, CpMenuItem item, int quantity) {
        this(order,item);
        this.quantity = quantity;
    }

    public OrderItem(CwSessionOrder order, CpMenuItem item) {
        id.setOrder(order);
        id.setItem(item);
    }

    public OrderItemKey getId() {
        return id;
    }

    public void setId(OrderItemKey id) {
        this.id = id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
