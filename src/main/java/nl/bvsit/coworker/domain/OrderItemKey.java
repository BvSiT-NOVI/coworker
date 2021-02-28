package nl.bvsit.coworker.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode
public class OrderItemKey implements Serializable {

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne
    @JoinColumn(name = "cs_order_id")
    private CwSessionOrder order;


    @ManyToOne
    @JoinColumn(name = "cp_menu_item_id")
    private CpMenuItem item;

    // standard constructors, getters, and setters
    // hashcode and equals implementation

}
