package nl.bvsit.coworker.payload;

import nl.bvsit.coworker.config.CwConstants;
import nl.bvsit.coworker.domain.CpMenuItem;
import nl.bvsit.coworker.domain.CwSession;
import nl.bvsit.coworker.domain.CwSessionOrder;
import nl.bvsit.coworker.domain.Seat;

//Builds  a CwSession with minimal required data to save as a new CwSession
public class CwSessionBuilder {
    private CwSessionDTO cwSessionDTO;
    public CwSessionBuilder(CwSessionDTO cwSessionDTO) {
        this.cwSessionDTO = cwSessionDTO;
    }

    public CwSession build(){
        CwSession cwSession = new CwSession();
        Seat seat = new Seat();
        if (cwSessionDTO.getSeat()!=null) seat.setId(cwSessionDTO.getSeat().getId());
        cwSession.setSeat(seat);
        for(CwSessionOrderDTO sessionOrderDTO: cwSessionDTO.getOrders()){
            CwSessionOrder cwSessionOrder = new CwSessionOrder();
            for(OrderItemDTO orderItemDTO: sessionOrderDTO.getMenuitems()){
                CpMenuItem cpMenuItem=new CpMenuItem();
                cpMenuItem.setId(orderItemDTO.getId());
                cwSessionOrder.addMenuItem(cpMenuItem,orderItemDTO.getQuantity());
            }
            cwSession.addCwSessionOrder(cwSessionOrder);
        }
        if (cwSession.getCwSessionOrders().size()!= CwConstants.NUM_ORDERS_PER_SESSION) throw new RuntimeException("Illegal number of orders.");
        return cwSession;
    }

}

