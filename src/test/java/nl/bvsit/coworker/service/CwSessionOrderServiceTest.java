package nl.bvsit.coworker.service;

import nl.bvsit.coworker.config.LogUtil;
import nl.bvsit.coworker.domain.CwSession;
import nl.bvsit.coworker.domain.CwSessionOrder;
import nl.bvsit.coworker.domain.OrderItem;
import nl.bvsit.coworker.payload.CpMenuItemDTO;
import nl.bvsit.coworker.payload.CwSessionOrderDTO;
import nl.bvsit.coworker.payload.OrderItemDTO;
import nl.bvsit.coworker.repository.CpMenuItemRepository;
import nl.bvsit.coworker.repository.CwSessionOrderRepository;
import nl.bvsit.coworker.repository.CwSessionRepository;
import nl.bvsit.coworker.repository.OrderItemRepository;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest()
public class CwSessionOrderServiceTest {
    private static final Logger logger =  LoggerFactory.getLogger(CwSessionOrderServiceTest.class);

    @Autowired
    CwSessionRepository cwSessionRepository;
    @Autowired
    CwSessionOrderRepository cwSessionOrderRepository;
    @Autowired
    CpMenuItemRepository cpMenuItemRepository;
    @Autowired
    CwSessionOrderService cwSessionOrderService;
    @Autowired
    CpMenuItemService cpMenuItemService;
    @Autowired
    OrderItemRepository orderItemRepository;

    @Transactional
    @Test
    void whenExecuting_updateCpMenuItems_thenIsSavedCorrectly(){
        //Arrange
        CwSession savedCwSession = cwSessionRepository.findByIdFetchAll(1L).orElse(null);
        assertNotNull(savedCwSession);

        //Find an order in the session which has not been served yet
        CwSessionOrder cwSessionOrder=null;
        for(CwSessionOrder order: savedCwSession.getCwSessionOrders()){
            if (order.getTimeServed()==null) {
                cwSessionOrder=order;
            }
        }
        assertNotNull(cwSessionOrder);
        CwSessionOrderDTO orderDtoToUpdate = cwSessionOrderService.toDto(cwSessionOrder);
        logger.info("Order before update:");
        LogUtil.logMappedObject(orderDtoToUpdate,logger,true,true);

        //get ids of all existing menu items
        List<Long> cpMenuItemIds = CpMenuItemUtil.getIds(cpMenuItemRepository.findAll());

        //Act

        //Remove all menu items in CwSessionOrder and replace by random existing menu items
        orderDtoToUpdate.getMenuitems().clear();
        List<Long> addedMenuItemIds=new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            //select random an id of an existing menu item
            Long randomId= cpMenuItemIds.get(new Random().nextInt(cpMenuItemIds.size()));
            if (!addedMenuItemIds.contains(randomId)){ //add only unique ids of menu items to the order
                CpMenuItemDTO cpMenuItemDTO = cpMenuItemService.getCpMenuItemDTO(randomId);
                OrderItemDTO orderItemDTO = new OrderItemDTO(cpMenuItemDTO,1);
                orderDtoToUpdate.addOrderItemDTO(orderItemDTO);
                addedMenuItemIds.add(randomId); //save id to compare
            }
        }

        logger.info("addedMenuItemIds="+addedMenuItemIds.toString());

        //Save the updated order
        CwSessionOrderDTO updatedDTO = cwSessionOrderService.updateOrderItems(cwSessionOrder.getId(), orderDtoToUpdate);

        logger.info("Updated order:");
        LogUtil.logMappedObject(updatedDTO,logger,true,true);

        //Assert

        //update of the CwSessionOrder should return a CwSessionOrderDTO with the same item id's
        List<Long> resultCpMenuItemIds = updatedDTO.getMenuitems()
                .stream().map(CpMenuItemDTO::getId)
                .collect(Collectors.toList());
        Collections.sort(resultCpMenuItemIds);
        logger.info("resultCpMenuItemIds ="+resultCpMenuItemIds);
        Collections.sort(addedMenuItemIds);
        logger.info("addedMenuItemIds ="+addedMenuItemIds);
        assertThat(resultCpMenuItemIds,is(addedMenuItemIds));
    }

}
