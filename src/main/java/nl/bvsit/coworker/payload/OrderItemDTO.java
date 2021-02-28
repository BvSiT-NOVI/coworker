package nl.bvsit.coworker.payload;

import javax.validation.constraints.Min;
import java.math.BigDecimal;

public class OrderItemDTO extends CpMenuItemDTO {
    @Min(1)
    private int quantity=1;

    public OrderItemDTO() {
    }

    public OrderItemDTO(Long id, String name, BigDecimal price, int quantity) {
        super(id, name, price);
        this.quantity = quantity;
    }

    public OrderItemDTO(CpMenuItemDTO cpMenuItemDTO, int quantity) {
        super(cpMenuItemDTO.getId(), cpMenuItemDTO.getName(), cpMenuItemDTO.getPrice() );
        this.quantity = quantity;
    }

    public OrderItemDTO(Long idMenuItem){
        super(idMenuItem, null, null );
    }

    public OrderItemDTO(Long idMenuItem, int quantity){
        super(idMenuItem, null, null);
        this.quantity=quantity;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
