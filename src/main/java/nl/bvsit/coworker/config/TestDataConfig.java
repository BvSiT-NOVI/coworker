package nl.bvsit.coworker.config;

import nl.bvsit.coworker.domain.*;
import nl.bvsit.coworker.exceptions.RecordNotFoundException;
import nl.bvsit.coworker.repository.*;
import nl.bvsit.coworker.service.CpMenuItemUtil;
import nl.bvsit.coworker.service.CwSessionOrderService;
import nl.bvsit.coworker.service.CwSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static nl.bvsit.coworker.config.CwConstants.NUM_ORDERS_PER_SESSION;

@Component
@Order(2)
public class TestDataConfig implements CommandLineRunner {
    private static final Logger logger =  LoggerFactory.getLogger(TestDataConfig.class);
    final static boolean VERBOSE=true;

    @Autowired
    private CwSessionRepository cwSessionRepository;
    @Autowired
    private CwSessionOrderRepository cwSessionOrderRepository;
    @Autowired
    private CpMenuItemRepository cpMenuItemRepository;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    CwSessionService cwSessionService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    CwSessionOrderService cwSessionOrderService;
    @Autowired
    OrderItemRepository orderItemRepository;

    @Override
    public void run(String... args)  {
        logger.info("Running "+ this.getClass().getName());
        createCpMenuItems(true);
        int numSeats = createSeats(true);
        boolean createRandomizedSession=false;
        if (CwConstants.LOAD_MINIMAL_TEST_DATA && CwConstants.CREATE_TEST_USERS ){
            List<User> coworkers = userRepository.findByRole(ERole.ROLE_COWORKER);
            if (coworkers.isEmpty()) throw new RuntimeException("No coworker user available to create a test session");
            List<User> employees = userRepository.findByRole(ERole.ROLE_EMPLOYEE);
            if (employees.isEmpty()) logger.info("No employee available while creating test session");
            CwSession cwSession = createCwSession(
                    coworkers.get(0).getUsername(),
                    employees.isEmpty()?null:employees.get(0).getUsername(),
                    0,
                    createRandomizedSession,
                    false );

            cwSession = cwSessionRepository.findByIdFetchAll(cwSession.getId()).orElse(null);
            logger.info("Created a " +  (createRandomizedSession?"randomized ":"") +  "test session:");
            LogUtil.logMappedObject(cwSessionService.toDtoFetchAll(cwSession),logger,false,false);
        }
        else if ( CwConstants.CREATE_TEST_USERS){
            createRandomCwSessions(numSeats,true);
        }
        infoSeats(); //free seats
    }

    void createCpMenuItems(boolean verbose){
        List<CpMenuItem> itemList = new ArrayList<>();
        for (int i=1;i<11;i++){
            CpMenuItem item = new CpMenuItem();
            item.setName("menuitem "+i);
            item.setPrice( new BigDecimal(i * 1.1));
            itemList.add(item);
        }
        cpMenuItemRepository.saveAll(itemList);
        if (verbose) {
            logger.info("Created and saved CpMenuItems :");
            cpMenuItemRepository.findAll().forEach((item)->LogUtil.logMappedObject(item,logger));
        }
    }

    int createSeats(boolean verbose){
        Map<String,String> data= new TreeMap<>();
        data.put("Tafel 1","Bij binnenkomst eerste tafel links");
        data.put("Tafel 2","Bij binnenkomst tweede tafel links");
        data.put("Tafel 3","Bij binnenkomst eerste tafel rechts");
        data.put("Tafel 4","Eerste tafel rechts van deur toiletten");
        data.put("Tafel 5","Hier zit je op de tocht, in de loop en te krap");

        for(String key: data.keySet()){
            Seat seat = new Seat(key,data.get(key));
            seatRepository.save(seat);
        }
        if (verbose){
            logger.info("Created and saved Seats :");
            if (verbose) seatRepository.findAll().forEach((seat)->LogUtil.logMappedObject(seat,logger));
        }
        return data.size();
    }

    void createRandomCwSessions(int count,boolean verbose){
        List<CwSession> savedCwSessions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            savedCwSessions.add(createRandomCwSession(false));
        }

        //Add for some CwSessions the endTime.
        for (int i = 0; i < (savedCwSessions.size()/2); i++) {
            CwSession cwSession = savedCwSessions.get(i);
            cwSession.setEndTime(LocalDateTime.now());
            cwSessionRepository.save(cwSession);
        }
        if (verbose) {
            logger.info("Created and saved random CwSessions :");
            cwSessionRepository.findAll().forEach( cwSession ->
            {cwSession = cwSessionRepository.findByIdFetchAll(cwSession.getId()).orElse(null);
                LogUtil.logMappedObject(cwSession,logger);} );
        }
    }

    public CwSession createRandomCwSession(boolean verbose){
        Random random=new Random();
        CwSession cs = new CwSession();

        //get random free Seat
        List<Seat> freeSeats = cwSessionService.findFreeSeats();
        cs.setSeat(freeSeats.get(random.nextInt(freeSeats.size())));

        //get ids of all existing menu items
        List<Long> cpMenuItemIds = CpMenuItemUtil.getIds(cpMenuItemRepository.findAll());

        Set<OrderItem> orderItems= new HashSet<>();
        for (int i = 0; i < NUM_ORDERS_PER_SESSION; i++) {
            CwSessionOrder  so = new CwSessionOrder();
            int max = random.nextInt(3)+1;//max 3 menuitems in an order
            List<Long> idsOfAddedCpMenuItems = new ArrayList<>();

            for(long j=1;j<= max;j++){
                Long randomId= cpMenuItemIds.get(random.nextInt(cpMenuItemIds.size()));
                //logger.info("j=" + j + " randomId = " + randomId);
                if (!idsOfAddedCpMenuItems.contains(randomId)) {
                    idsOfAddedCpMenuItems.add(randomId);
                    CpMenuItem cpMenuItem = cpMenuItemRepository.findById(randomId).orElseThrow(RecordNotFoundException::new);
                    int quantity = random.nextInt(2)+1;//Random quantity between 1 and 2
                    orderItems.add(so.addMenuItem(cpMenuItem,quantity));
                }
            }
            cs.addCwSessionOrder(so);
        }
        cwSessionRepository.save(cs);
        orderItemRepository.saveAll (cwSessionService.getOrderItems(cs));
        if (verbose) cwSessionRepository.findByIdFetchAll(cs.getId()).ifPresent(
                cwSession -> {
                    logger.info("Created and saved CwSession:");
                    LogUtil.logMappedObject(cwSession, logger);
                }
        );
        return cs;
    }

    public void infoSeats(){
        logger.info("Free seats:");
        cwSessionService.findFreeSeats().forEach(seat-> LogUtil.logMappedObject(seat,logger));
    }

    public CwSession createCwSession(String userNameCoworker,
                                     String userNameServingEmployee,
                                     int numOrdersServed,
                                     boolean randomize,
                                     boolean verbose){
        CwSession cwSession = (randomize)?createRandomCwSession(false):createCwSession(false);

        //Add coworker to session
        User coworker =  userRepository.findByUsername(userNameCoworker)
                .orElseThrow(()-> new  RuntimeException("User "+ userNameCoworker + " not found while loading test data"));
        cwSession.setUser(coworker);
        cwSession.setStartTime(LocalDateTime.now());
        cwSession = cwSessionRepository.save(cwSession);

        //Serve orders
        int numOrders = 0;
        if (numOrdersServed>0){
            numOrders= Math.min(numOrdersServed, NUM_ORDERS_PER_SESSION);
        }
        if (numOrders>0){
            if (userNameServingEmployee!=null){
                User employee =  userRepository.findByUsername(userNameServingEmployee)
                        .orElseThrow(()-> new  RuntimeException("User "+ userNameCoworker + " not found while loading test data"));
                for (int i = 0; i < numOrdersServed; i++) {
                    cwSessionOrderService.setServedCwSessionOrder(
                            cwSession.getCwSessionOrders().get(i).getId(),employee);
                }
            }
        }
        return cwSession;

    }

    public CwSession createCwSession(boolean verbose){
        //Creates and saves CwSession with fixed Seat and CpMenuItems
        CwSession cs = new CwSession();
        List<Seat> freeSeats = cwSessionService.findFreeSeats();
        if (freeSeats.isEmpty()) {
            logger.info("No free seat available, unable to create session");
            return null;
        }
        cs.setSeat(freeSeats.get(0));

        Set<OrderItem> orderItems= new HashSet<>();

        for (int i = 0; i < NUM_ORDERS_PER_SESSION; i++) {
            CwSessionOrder  so = new CwSessionOrder();
            for(int j=1;j<=2;j++){
                CpMenuItem cpMenuItem  = cpMenuItemRepository.findById((long) j).orElseThrow(RecordNotFoundException::new);
                orderItems.add(so.addMenuItem(cpMenuItem,j));
            }
            cs.addCwSessionOrder(so);
        }

        cs = cwSessionRepository.save(cs);
        orderItemRepository.saveAll(orderItems);
        //Without next line ObjectMapper throws com.fasterxml.jackson.databind.JsonMappingException: could not initialize proxy [nl.bvsit.coworker.domain.Seat#1] - no Session (through reference
        cs = cwSessionRepository.findByIdFetchAll(cs.getId()).get();
        if (verbose) LogUtil.logMappedObject(cs,logger);
        return cs;
    }

    void createCwSessions(){
        List<CwSession> list = new ArrayList<>();
        for (int i=1;i<11;i++){
            CwSession cs = new CwSession();
            list.add(cs);
        }
        cwSessionRepository.saveAll(list);
        cwSessionRepository.findAll().forEach(
                (cs)->logger.info("Created session with id:"+ cs.getId())
        );
    }
}


