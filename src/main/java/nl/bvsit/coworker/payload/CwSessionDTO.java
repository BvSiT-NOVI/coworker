package nl.bvsit.coworker.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.ToString;
import nl.bvsit.coworker.config.CwConstants;
import nl.bvsit.coworker.customvalidator.SizeOrdersConstraint;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@ToString
public class CwSessionDTO {
    @Min(value = 1)
    private Long id;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime startTime;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime endTime;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime paymentTime;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Digits(integer=3, fraction=2)
    private BigDecimal total;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean closed;
    private SeatDTO seat;
    @JsonProperty(value = "user_id",access = JsonProperty.Access.READ_ONLY)
    private Long userId;

    @SizeOrdersConstraint(set_size = CwConstants.NUM_ORDERS_PER_SESSION)
    private List<CwSessionOrderDTO> orders = new ArrayList<>();

    //utility
    public void addOrder(CwSessionOrderDTO cwSessionOrderDTO){
        orders.add(cwSessionOrderDTO);
    }

    //custom getter

    public List<CwSessionOrderDTO> getOrders() {
        return orders.stream()
                .sorted(Comparator.comparing(CwSessionOrderDTO::getRank))
                .collect(Collectors.toList());
    }

    //getters and setters
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

    public void setOrders(List<CwSessionOrderDTO> orders) {
        this.orders = orders;
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

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public SeatDTO getSeat() {
        return seat;
    }

    public void setSeat(SeatDTO seat) {
        this.seat = seat;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
