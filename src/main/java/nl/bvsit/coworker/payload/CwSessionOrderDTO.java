package nl.bvsit.coworker.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.ToString;

import javax.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@ToString
public class CwSessionOrderDTO {
    @Min(value = 1)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private int rank;
	@JsonProperty(value="time_to_serve",access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime timeToServe;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)								  
    private LocalDateTime timeServed;
    @JsonProperty(value = "served_by",access = JsonProperty.Access.READ_ONLY)
    private Long employeeId;

    @Min(1)
    private Set<OrderItemDTO> menuitems=new HashSet(); //NB Name external exposure, maps to CwSessionOrder.cpMenuItems

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public void addOrderItemDTO(OrderItemDTO menuItem){menuitems.add(menuItem); }

    public void removeOrderItemDTO(OrderItemDTO menuItem){
        menuitems.remove(menuItem);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getTimeServed() {
        return timeServed;
    }

    public void setTimeServed(LocalDateTime timeServed) {
        this.timeServed = timeServed;
    }

    public Set<OrderItemDTO> getMenuitems() {
        return menuitems;
    }

    public void setMenuitems(Set<OrderItemDTO> menuitems) {
        this.menuitems = menuitems;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

	public LocalDateTime getTimeToServe() {
        return this.timeToServe;
    }

    public void setTimeToServe(LocalDateTime timeToServe) {
        this.timeToServe = timeToServe;
    }										   
}
