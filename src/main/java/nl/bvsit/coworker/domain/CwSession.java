package nl.bvsit.coworker.domain;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="cw_session")
public class CwSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime paymentTime;
	@ManyToOne
    private User employeePaidTo;	
    private boolean closed;

    @Transient
    private BigDecimal total;

    @ManyToOne
    private User user;

    @OrderColumn(name="list_order")
    @OneToMany(mappedBy="cwSession",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<CwSessionOrder> cwSessionOrders = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(foreignKey = @ForeignKey(name="fk_cw_session__seat") )
    private Seat seat;

    //utility methods

    public void addCwSessionOrder(CwSessionOrder cwSessionOrder){
        cwSessionOrders.add(cwSessionOrder);
        if (cwSessionOrder.getCwSession()!=this){
            cwSessionOrder.setCwSession(this);
        }
    }

    //constructor

    public CwSession() {
    }

    //custom setter
    public void setCwSessionOrders(List<CwSessionOrder> cwSessionOrders) {
        this.cwSessionOrders = cwSessionOrders;
        //NB Add this!! https://hellokoding.com/jpa-one-to-many-relationship-mapping-example-with-spring-boot-maven-and-mysql/
        for(CwSessionOrder order : cwSessionOrders) {
            order.setCwSession(this);
        }
    }

    //getters and setters

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public LocalDateTime getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(LocalDateTime paymentTime) {
        this.paymentTime = paymentTime;
    }

    public List<CwSessionOrder> getCwSessionOrders() {
        return cwSessionOrders;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public Seat getSeat() {
        return seat;
    }

    public void setSeat(Seat seat) {
        this.seat = seat;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
	    public User getEmployeePaidTo() {
        return employeePaidTo;
    }

    public void setEmployeePaidTo(User employeePaidTo) {
        this.employeePaidTo = employeePaidTo;
    }								 
}
