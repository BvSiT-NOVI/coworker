package nl.bvsit.coworker.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "cp_menu_item")
public class CpMenuItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "price")
    private BigDecimal price;

    @JsonIgnore
    @OneToMany(mappedBy = "id.item")
    Set<OrderItem> orderItems=new HashSet<>();

    //constructor
    public CpMenuItem() {
    }

    public CpMenuItem(Long id) {
        this.id = id;
    }

    public CpMenuItem(String name, BigDecimal price) {
        this.name = name;
        this.price = price;
    }

}
