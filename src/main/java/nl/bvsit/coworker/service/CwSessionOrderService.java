package nl.bvsit.coworker.service;

import nl.bvsit.coworker.domain.*;
import nl.bvsit.coworker.exceptions.BadRequestException;
import nl.bvsit.coworker.exceptions.RecordNotFoundException;
import nl.bvsit.coworker.exceptions.UpdateException;
import nl.bvsit.coworker.payload.CwSessionOrderDTO;
import nl.bvsit.coworker.payload.OrderItemDTO;
import nl.bvsit.coworker.repository.CpMenuItemRepository;
import nl.bvsit.coworker.repository.CwSessionOrderRepository;
import nl.bvsit.coworker.repository.OrderItemRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Validated
@Service
public class CwSessionOrderService {
    @Autowired
    private CpMenuItemRepository cpMenuItemRepository;
    @Autowired
    private CwSessionOrderRepository cwSessionOrderRepository;
    @Autowired
    private ModelMapper modelMapper; //NB See @Bean in DemoApplication
    @Autowired
    CwSessionService cwSessionService;
    @Autowired
    CwUserService cwUserService;
    @Autowired
    OrderItemRepository orderItemRepository;

    public CwSessionOrderDTO setServedCwSessionOrder(Long orderId,User user) {
        CwSessionOrder cwSessionOrder = cwSessionOrderRepository.findById(orderId).orElseThrow(RecordNotFoundException::new);
        if (cwSessionOrder.getTimeServed() == null) {
            cwSessionOrder.setTimeServed(LocalDateTime.now());
            cwSessionOrder.setServedBy(user);
            cwSessionOrder = cwSessionOrderRepository.save(cwSessionOrder);
        }
        return toDto(cwSessionOrder);
    }

    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE','ROLE_MANAGER','ROLE_ADMIN')")
    public CwSessionOrderDTO setServedCwSessionOrderAuthenticated(Long orderId,UserDetailsImpl authUser) {
        User userServedBy = cwUserService.validatedUser(authUser);
        return setServedCwSessionOrder(orderId,userServedBy);
    }

    public CwSessionOrderDTO updateOrderItems(Long orderId,CwSessionOrderDTO orderDTO) {
        CwSessionOrder existingCwSessionOrder = validatedCwSessionOrder(orderId,true);
        if (existingCwSessionOrder.getTimeServed() != null) throw new UpdateException("Unable to update an order which has been served.");
        idAnyNonExistingCpMenuItem(orderDTO).ifPresent(id -> {throw new RecordNotFoundException("Record not found with id "+id);});
        List<OrderItem> orderItems = orderItemRepository.findByOrderWhereIdIs(existingCwSessionOrder.getId());
        orderItemRepository.deleteAll(orderItems);
        existingCwSessionOrder = validatedCwSessionOrder(orderId,true); //!
        existingCwSessionOrder.getOrderItems().clear();//?
        for(OrderItemDTO orderItemDTO: orderDTO.getMenuitems()){
            CpMenuItem cpMenuItem = cpMenuItemRepository.findById(orderItemDTO.getId()).get();
            existingCwSessionOrder.addMenuItem(cpMenuItem,orderItemDTO.getQuantity());
        }
        orderItemRepository.saveAll(existingCwSessionOrder.getOrderItems());
        return toDto(cwSessionOrderRepository.save(existingCwSessionOrder)) ;
    }

    public CwSessionOrderDTO updateCpMenuItemsX(Long orderId,CwSessionOrderDTO orderDTO) {//DEBUG
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.LOOSE); //map .orders -> .cwSessionOrders etc.
        orderDTO.setId(orderId);
        CwSessionOrder cwSessionOrderFromDTO =  modelMapper.map(orderDTO,CwSessionOrder.class);
        CwSessionOrder existingCwSessionOrder = validatedCwSessionOrder(orderId,true);
        if (existingCwSessionOrder.getTimeServed() != null) throw new UpdateException("Unable to update an order which has been served.");
        idAnyNonExistingCpMenuItem(cwSessionOrderFromDTO).ifPresent(id -> {throw new RecordNotFoundException("Record not found with id "+id);});
        cwSessionOrderFromDTO.setCwSession(existingCwSessionOrder.getCwSession()); //!
        return toDto(cwSessionOrderRepository.save(cwSessionOrderFromDTO)) ;
    }

    public CwSessionOrderDTO updateOrderAuthenticated(Long orderId, CwSessionOrderDTO orderDTO,UserDetailsImpl authUser){
        User user = cwUserService.validatedUser(authUser);
        CwSessionOrder cwSessionOrder = validatedCwSessionOrder(orderId,true);
        if (user.hasRole(ERole.ROLE_COWORKER) && user.getRoles().size()==1){
            if (!user.getId().equals(cwSessionOrder.getCwSession().getUser().getId())) {
                throw new BadRequestException(); //if order does not belong to the coworker
            }
        }
        return updateOrderItems(orderId,orderDTO);
    }

    private Optional<Long> idAnyNonExistingCpMenuItem(CwSessionOrder cwSessionOrder){
        List<Long> menuItemIds = CpMenuItemUtil.getMenuItemIds(cwSessionOrder.getOrderItems());
        return idAnyNonExistingCpMenuItem(menuItemIds);
    }

    private Optional<Long> idAnyNonExistingCpMenuItem(CwSessionOrderDTO orderDTO){
        List<Long> menuItemIds = CpMenuItemUtil.getOrderItemDtoIds(orderDTO.getMenuitems());
        return idAnyNonExistingCpMenuItem(menuItemIds);
    }

    private Optional<Long> idAnyNonExistingCpMenuItem(List<Long> menuItemIds){
        List<Long> idsOfSavedMenuItems = CpMenuItemUtil.getIds(cpMenuItemRepository.findByIdIn(menuItemIds));
        menuItemIds.removeAll(idsOfSavedMenuItems);// Remove all items that exist in the database
        if (menuItemIds.size()>0){
            return Optional.of(menuItemIds.get(0)); //Return first id of menuItem in order which is never saved to the database.
        }
        return Optional.empty();
    }

    public CwSessionOrderDTO toDto(CwSessionOrder cwSessionOrder){
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.LOOSE);
        CwSessionOrderDTO orderDTO = modelMapper.map(cwSessionOrder, CwSessionOrderDTO.class);
        List<OrderItem> orderItems = orderItemRepository.findByOrderWhereIdIs(cwSessionOrder.getId());
        for(OrderItem item: orderItems){
            OrderItemDTO orderItemDTO =modelMapper.map(item.getId().getItem(), OrderItemDTO.class);
            orderItemDTO.setQuantity(item.getQuantity());
            orderDTO.addOrderItemDTO(orderItemDTO);
        }
        return resetEmployeeNonServedOrder(orderDTO);
    }

    public CwSessionOrderDTO resetEmployeeNonServedOrder(CwSessionOrderDTO orderDTO){
        //ObjectMapper, when mapping a CwSessionOrder to CwSessionOrderDTO,
        //will erroneously write id of order to .employeeId, so reset. See also CwSessionService.toDtoFetchAll()
        if (orderDTO.getTimeServed()==null){
            orderDTO.setEmployeeId(null);
        }
        return  orderDTO;
    }

    public CwSessionOrder validatedCwSessionOrder(Long orderId,boolean returnObject){
        if (! cwSessionOrderRepository.existsById(orderId)) throw new RecordNotFoundException();
        //Only fetch the order if necessary
        if (returnObject) return cwSessionOrderRepository.findById(orderId).orElse(null);
        return null;
    }																				 
}
