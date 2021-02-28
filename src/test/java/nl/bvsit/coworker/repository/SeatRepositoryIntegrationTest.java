package nl.bvsit.coworker.repository;

import nl.bvsit.coworker.config.TestDataConfig;
import nl.bvsit.coworker.domain.CwSession;
import nl.bvsit.coworker.domain.Seat;
import nl.bvsit.coworker.service.CwSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.ANY)  //Uses H2 database
public class SeatRepositoryIntegrationTest {
    private static final Logger logger =  LoggerFactory.getLogger(SeatRepositoryIntegrationTest.class);
    @Autowired
    SeatRepository seatRepository;
    @Autowired
    CwSessionRepository cwSessionRepository;
    @Autowired
    TestDataConfig testDataConfig;
    @Autowired
    CwSessionService cwSessionService;

    CwSession cwSession;

    @BeforeEach
    void init(){
        cwSession = testDataConfig.createCwSession(true);//creates and saves CwSession with Seat with id = 1
        assertNotNull(cwSession);
    }

    @Test
    void whenAddingASeatToASession_thenFreeSeatsDecreaseByOne(){
        //Arrange
        List<Seat> seatList =  cwSessionService.findFreeSeats();
        assertTrue(seatList.size()>0);
        seatList.forEach(s->logger.info(s.getCode()));
        int numFreeSeats = seatList.size();

        //Act
        cwSession = testDataConfig.createCwSession(true); //create new CwSession
        cwSession.setSeat(seatList.get(0)); //Add a free Seat to the session diminishing the number of free Seats by 1
        cwSessionRepository.save(cwSession);
        int expected = numFreeSeats-1;

        //Assert

        int actual = cwSessionService.findFreeSeats().size();
        assertEquals(expected,actual);

    }


}
