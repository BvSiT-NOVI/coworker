package nl.bvsit.coworker.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.Formula;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "cs_order")
public class CwSessionOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Formula("list_order")
    private int rank;
    private LocalDateTime timeServed;
    @Transient
    LocalDateTime timeToServe;

    @ManyToOne
    private User servedBy;		
	
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "cw_session_id", referencedColumnName = "id",
            foreignKey = @ForeignKey(name="fk_cs_order__cw_session") )
    CwSession cwSession;
    @OneToMany(mappedBy = "id.order",fetch = FetchType.EAGER)
    Set<OrderItem> orderItems= new HashSet<>();


    //Constructors
    public CwSessionOrder() {}

    public CwSessionOrder(Long id) {
        this.id = id;
    }

    public CwSessionOrder(int rank, LocalDateTime timeServed, CwSession cwSession, Set<OrderItem> orderItems) {
        this.rank = rank;
        this.timeServed = timeServed;
        this.cwSession = cwSession;
        this.orderItems = orderItems;
    }

    //Utility method, see https://stackoverflow.com/questions/23837561/jpa-2-0-many-to-many-with-extra-column
    public OrderItem addMenuItem(CpMenuItem cpMenuItem,int quantity){
        OrderItem orderItem = new OrderItem();
        orderItem.setQuantity(quantity);
        orderItem.id.setItem(cpMenuItem);
        orderItem.id.setOrder(this);
        this.orderItems.add(orderItem);
        return orderItem;
    }

    //Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public LocalDateTime getTimeServed() {
        return timeServed;
    }

    public void setTimeServed(LocalDateTime timeServed) {
        this.timeServed = timeServed;
    }

    public CwSession getCwSession() {
        return cwSession;
    }

    public void setCwSession(CwSession cwSession) {
        this.cwSession = cwSession;
    }

    public Set<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(Set<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public User getServedBy() {
        return servedBy;
    }

    public void setServedBy(User servedBy) {
        this.servedBy = servedBy;
    }
	
	    public LocalDateTime getTimeToServe() {
        return timeToServe;
    }

    public void setTimeToServe(LocalDateTime timeToServe) {
        this.timeToServe = timeToServe;
    }
}
