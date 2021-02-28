package nl.bvsit.coworker.service;

import nl.bvsit.coworker.config.CwConstants;
import nl.bvsit.coworker.domain.*;
import nl.bvsit.coworker.exceptions.BadRequestException;
import nl.bvsit.coworker.exceptions.RecordNotFoundException;
import nl.bvsit.coworker.payload.CwSessionBuilder;
import nl.bvsit.coworker.payload.CwSessionDTO;
import nl.bvsit.coworker.payload.CwSessionOrderDTO;
import nl.bvsit.coworker.payload.OrderItemDTO;
import nl.bvsit.coworker.repository.*;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static nl.bvsit.coworker.config.CwConstants.MENU_ITEM_NOT_FOUND_WITH_ID;

@Validated
@Service
public class CwSessionService {
    private static final Logger logger =  LoggerFactory.getLogger(CwSessionService.class); //debug
    @Autowired
    private CpMenuItemRepository cpMenuItemRepository;
    @Autowired
    private CwSessionRepository cwSessionRepository;
    @Autowired
    private CwSessionOrderRepository cwSessionOrderRepository;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private CwUserService cwUserService;
    @Autowired
    private ModelMapper modelMapper; //NB See @Bean in DemoApplication

    public CwSessionDTO createCwSession(@Valid CwSessionDTO cwSessionDTO, Long coworkerId) {
        //Note. If coworker has a session with endtime but not paid yet, he can still create a new session. This is by design.
        User coworker = cwUserService.validatedUser(coworkerId,ERole.ROLE_COWORKER);
        if (cwSessionRepository.existsByUserAndEndTimeIsNullAndClosedIsFalse(coworker)) throw new BadRequestException("User has already an active session");
        CwSession cwSession = new CwSessionBuilder(cwSessionDTO).build(); //Note that the correct number of orders is validated in the DTO
        Seat validatedSeat = validatedSeat(cwSession.getSeat()); //Checks a.o. if Seat is available. Returns fully fetched Seat object
        validateMenuItems(cwSession); //Ensure that all menu items exist.
        cwSession.setUser(coworker);
        cwSession.setStartTime(LocalDateTime.now());
        cwSession = cwSessionRepository.save(cwSession);
        saveOrderItems(cwSession);
        //Refetch complete session. Without this rank is not set.
        cwSession = cwSessionRepository.findByIdFetchAll(cwSession.getId()).orElseThrow(RecordNotFoundException::new);
        //NB All is saved correctly but the CwSession object returned by .save() does not contain full CpMenuItem's caused by Lazy initialization
        return toDtoFetchAll(cwSession); // Fetches all CpMenuItem instance variables
    }

    public CwSessionDTO createCwSessionAuthenticated(@Valid CwSessionDTO cwSessionDTO,UserDetailsImpl authUser){
        User user = cwUserService.validatedUser(authUser,ERole.ROLE_COWORKER);
        return createCwSession(cwSessionDTO, user.getId());
    }

    public CwSessionDTO endCwSession(Long id) {
        CwSession cwSession = validatedCwSession(id);
        if (cwSession.isClosed())  throw new BadRequestException("Error: session is closed.");
        if (cwSession.getEndTime()!=null)  throw new BadRequestException("Error: session has already ended.");
        cwSession.setEndTime(LocalDateTime.now());
        cwSession = cwSessionRepository.save(cwSession);
        return toDtoFetchAll(cwSession);
    }

    public CwSessionDTO endCwSessionAuthenticated(Long sessionId,UserDetailsImpl authUser){
        CwSession cwSession = validatedCwSession(sessionId);
        //Both coworkers and employees are allowed to end session
        User user = cwUserService.validatedUser(authUser,ERole.ROLE_COWORKER,ERole.ROLE_EMPLOYEE);
        if (user.hasRole(ERole.ROLE_COWORKER)){
            if (!user.getId().equals(cwSession.getUser().getId())) {
                throw new BadRequestException(); //if session does not belong to the coworker
            }
        }
        return endCwSession(sessionId);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MANAGER','ROLE_EMPLOYEE')")
    public CwSessionDTO closeCwSession(Long sessionId){
        CwSession cwSession = validatedCwSession(sessionId);
        if (!cwSession.isClosed()) {
            cwSession.setClosed(true);
            cwSession = cwSessionRepository.save(cwSession);
        }
        return toDtoFetchAll(cwSession);
    }


    public CwSessionDTO setAsPaidCwSession(Long sessionId,User employeePaidTo){
        CwSession cwSession = validatedCwSession(sessionId);
        if (cwSession.isClosed()) throw new BadRequestException("Error: session is closed.");
        if (cwSession.getEndTime()==null) throw new BadRequestException("Error: session must be ended before payment.");
        if (cwSession.getPaymentTime()==null) {
            if (cwSession.getEndTime()==null) cwSession.setEndTime(LocalDateTime.now());
            cwSession.setEmployeePaidTo(employeePaidTo);
            cwSession.setPaymentTime(LocalDateTime.now());
            cwSession = cwSessionRepository.save(cwSession);
        }
        else{
            throw new BadRequestException("Error: session is already paid.");
        }
        return toDtoFetchAll(cwSession);
    }

    public CwSessionDTO setAsPaidCwSessionAuthenticated(Long sessionId,UserDetailsImpl authUser){
        User user = cwUserService.validatedUser(authUser,ERole.ROLE_EMPLOYEE,ERole.ROLE_MANAGER);
        validatedCwSession(sessionId);
        //Only employees and managers are allowed to mark session as paid
        return setAsPaidCwSession(sessionId,user);
    }

    public CwSessionDTO getCwSession(Long sessionId) {
        return toDtoFetchAll(validatedCwSession(sessionId));
    }

    public CwSessionDTO getCwSessionAuthenticated(Long sessionId,UserDetailsImpl authUser) {
        User user = cwUserService.validatedUser(authUser);//user exists with any role
        CwSession cwSession = validatedCwSession(sessionId);
        if (user.hasRole(ERole.ROLE_COWORKER) && user.getRoles().size()==1){
            if (!user.getId().equals(cwSession.getUser().getId())) {
                throw new BadRequestException(); //if session does not belong to the coworker
            }
        }
        return getCwSession(sessionId);
    }

    public Page<CwSessionDTO> getAllAsDTO(Pageable pageable, User user) {
        List<CwSession> cwSessionList;
        if (user!=null){
            cwSessionList =  cwSessionRepository.findByUser(user);
        }
        else {
            cwSessionList =  cwSessionRepository.findAll();
        }
        if (cwSessionList.size()==0) throw new RecordNotFoundException();
        List<CwSessionDTO> cwSessionDTOList=  cwSessionList.stream().map(this::toDtoFetchAll).collect(Collectors.toList());
        return new PageImpl<>(cwSessionDTOList, pageable, cwSessionDTOList.size());
    }

    public Page<CwSessionDTO> getAllAuthenticated(Pageable pageable,UserDetailsImpl authUser){
        User user = cwUserService.validatedUser(authUser);
        if (! (user.hasRole(ERole.ROLE_COWORKER) && user.getRoles().size()==1) ) user=null;//return all sessions
        return getAllAsDTO(pageable,user);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deleteCwSession(Long sessionId){
        CwSession cwSession = validatedCwSession(sessionId);
        orderItemRepository.deleteAll(getOrderItems(cwSession));
        cwSessionRepository.delete( validatedCwSession(sessionId) );
    }

    private Optional<Long> idAnyNonExistingCpMenuItem(CwSession cwSession){
        //Return id of any CpMenuItem in CwSession object which does not exist in the database
        List<Long> sessionCpMenuItemIds = getCpMenuItemIds(cwSession);
        List<Long> existingCpMenuItemIds =
                cpMenuItemRepository.findByIdIn(sessionCpMenuItemIds)
                    .stream().map(CpMenuItem::getId)
                    .collect(Collectors.toList());
        sessionCpMenuItemIds.removeAll(existingCpMenuItemIds);// Remove all items that exist in the database
        if (sessionCpMenuItemIds.size()>0){
            return Optional.of(sessionCpMenuItemIds.get(0)); //Return first id of CpMenuItem which is never saved to the database.
        }
        return Optional.empty();
    }

    public List<Long> getCpMenuItemIds(CwSession cwSession){
        return getOrderItems(cwSession).stream()
                .map(orderItem -> orderItem.getId().getItem().getId())
                .distinct()
                .collect(Collectors.toList());
    }

    public CwSessionDTO toDto(CwSession cwSession){
		calcTotal(cwSession);												   
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.LOOSE);
        return modelMapper.map(cwSession, CwSessionDTO.class);
    }

    public CwSessionDTO toDtoFetchAll(CwSession cwSession) {
        /*
         *  Converts CwSession to CwSessionDTO while ensuring that all menu items are fetched.
         *  CwSessionRepository.save() returns a CwSession object without these instance variables
         *  caused by lazy fetch relations.
         *  We created a custom query cwSessionRepository.findByIdWithAllChildren() using
         *  JOIN FETCH which returns successfully the full CwSession object in tests and in the DatabaseFiller
         *  But used in CwSessionService.createCwSession() this does not work??
         *  See also
         *  https://thorben-janssen.com/lazyinitializationexception/
         *  https://blog.ippon.fr/2017/07/19/boostez-performances-de-application-spring-data-jpa/
         *  @Transactional is also a quick fix to avoid lazy initialization exception.
         *  https://stackoverflow.com/questions/53836776/lazyinitializationexception-spring-boot
         *  The exception would also be avoided by setting all fetch types to EAGER (tested) but this
         *  probably causes bad performance by using multiple queries.
         */

        setTimeToServeInOrders(cwSession);
        CwSessionDTO cwSessionDTO = toDto(cwSession); //NB Still missing menu items

        for (CwSessionOrderDTO order : cwSessionDTO.getOrders()) {
            //While mapping the session to the DTO, ObjectMapper maps erroneously the id of the order to employeeId in the DTO, so correct always.
            idEmployeeServedBy(order.getId()).ifPresentOrElse(order::setEmployeeId,()->order.setEmployeeId(null));
            List<OrderItem> orderItems = orderItemRepository.findByOrderWhereIdIs(order.getId());
            for(OrderItem item: orderItems){
                OrderItemDTO orderItemDTO =modelMapper.map(item.getId().getItem(), OrderItemDTO.class);
                orderItemDTO.setQuantity(item.getQuantity());
                order.addOrderItemDTO(orderItemDTO);
            }
        }
        return cwSessionDTO;
    }

    public CwSession validatedCwSession(Long sessionId){
        return validatedCwSession(sessionId,true);
    }

    public boolean validateMenuItems(CwSession cwSession){
        //If true all menu items in the session have an id of an existing menu item
        idAnyNonExistingCpMenuItem(cwSession)
                .ifPresent(id -> {throw new RecordNotFoundException(String.format(MENU_ITEM_NOT_FOUND_WITH_ID,id));});
        return true;
    }

    public CwSession validatedCwSession(Long sessionId,boolean returnCwSession){
        if (! cwSessionRepository.existsById(sessionId)) throw new RecordNotFoundException();
        //Only fetch the session if necessary
        if (returnCwSession) return cwSessionRepository.findById(sessionId).orElse(null);
        return null;
    }

    public Seat validatedSeat(Seat seat) {
        if (seat == null) throw new BadRequestException("seat is null");
        if (seat.getId()==null) throw new BadRequestException("seat id is null");
        if (!seatRepository.existsById(seat.getId())) throw new RecordNotFoundException("seat does not exist.");
        if (cwSessionRepository.existsBySeatAndEndTimeIsNullAndClosedIsFalse(seat)) throw new BadRequestException("seat is not available.");
        return seatRepository.findById(seat.getId()).get(); //fetch all lazy fetched instance variables
    }

    public List<Seat> findFreeSeats(){
        List<Seat> freeSeats = new ArrayList<>();
        for (Seat seat: seatRepository.findAll()){
            if (cwSessionRepository.existsBySeatAndEndTimeIsNullAndClosedIsFalse(seat)) continue;
            freeSeats.add(seat);
        }
        return freeSeats;
    }

    public BigDecimal calcTotal(CwSession session){
        if (session.getEndTime()==null) return null;
        BigDecimal total= BigDecimal.ZERO;
        for(CwSessionOrder order:session.getCwSessionOrders()){
            if (order.getTimeServed()!=null){ //only menu items of served orders
                for(OrderItem orderItem :order.getOrderItems()){
                    total = total.add(
                            orderItem.getId().getItem()
                                    .getPrice()
                                    .multiply(BigDecimal.valueOf(orderItem.getQuantity()))
                    );
                }
            }
        }
        session.setTotal(total);
        return total;
    }

    public Optional<Long> getIdEmployeeServedBy(CwSessionOrderDTO orderDTO){
        if (orderDTO!=null){
            CwSessionOrder order = cwSessionOrderRepository.findById(orderDTO.getId()).orElse(null);
            if (order!=null ){
                User employeeServedBy = order.getServedBy();
                if (employeeServedBy!=null) return Optional.of(employeeServedBy.getId());
            }
        }
        return Optional.empty();
    }

    public Optional<Long> idEmployeeServedBy(Long idOrder){
        CwSessionOrder order = cwSessionOrderRepository.findById(idOrder).orElse(null);
        if (order!=null ){
            User employeeServedBy = order.getServedBy();
            if (employeeServedBy!=null) return Optional.of(employeeServedBy.getId());
        }
        return Optional.empty();
    }
    public boolean setTimeToServeInOrders(CwSession cwSession){
        LocalDateTime startTime= cwSession.getStartTime();
        if (startTime==null) return false;
        if (CwConstants.NUM_ORDERS_PER_SESSION>1){
            List<Integer> ranks = cwSession.getCwSessionOrders()
                    .stream().map(CwSessionOrder::getRank)
                    .distinct().collect(Collectors.toList());
            //If the rank is not set in the orders, ranks will consist of one element with value 0
            if (ranks.size() <= 1 ) return false;
        }
        for (CwSessionOrder order:cwSession.getCwSessionOrders()){
            LocalDateTime timeToServe= startTime.
                    plusMinutes( (CwConstants.ORDER_INTERVAL_IN_MINUTES * (order.getRank()+1)) );
            order.setTimeToServe(timeToServe);
        }
        return true;
    }

    public List<OrderItem> getOrderItems(CwSession cwSession){
        List<OrderItem> orderItems= new ArrayList<>();
        for(CwSessionOrder order :cwSession.getCwSessionOrders()){
            orderItems.addAll(order.getOrderItems());
        }
        return orderItems;
    }

    public void saveOrderItems(CwSession cwSession){
        for(CwSessionOrder order: cwSession.getCwSessionOrders()){
            orderItemRepository.saveAll(order.getOrderItems());
        }
    }
}
