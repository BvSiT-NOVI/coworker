package nl.bvsit.coworker.service;

import nl.bvsit.coworker.domain.ERole;
import nl.bvsit.coworker.domain.Role;
import nl.bvsit.coworker.domain.User;
import nl.bvsit.coworker.payload.request.SignupRequest;
import nl.bvsit.coworker.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Transactional //Important! Reverts all changes to the database
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) //use Postgres DB
public class AuthorizationServiceIntegrationTest {
    private static final Logger logger =  LoggerFactory.getLogger(AuthorizationServiceIntegrationTest.class);
    @Autowired
    AuthorizationService authorizationService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    CwSessionRepository cwSessionRepository;
    @Autowired
    OrderItemRepository orderItemRepository;

    @BeforeEach
    void init(){
        orderItemRepository.deleteAll();
        cwSessionRepository.deleteAll();
        for(User user:userRepository.findAll()){
            if (!user.hasRole(ERole.ROLE_ADMIN)) userRepository.delete(user);
        }
    }

    @Transactional
    @Test
    void when_registerAdminIsPassedRequestWithAdminUserAndAnotherUserWithAdminRoleExists_thenThrowsRuntimeException(){
        //Arrange
        userRepository.deleteAll();
        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN).get();
        assertNotNull(adminRole);
        User user = new User("admin","admin@novi.nl","password");
        user.addRole(adminRole);
        logger.info("Number of users: "+userRepository.findAll().size());
        userRepository.save(user);

        //Act
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("admin2");
        signupRequest.setPassword("password");
        signupRequest.setEmail("admin2@novi.nl");
        Set<String> roleNames = new HashSet<>();
        roleNames.add("admin");
        signupRequest.setRole(roleNames);

        //Assert
        Throwable assertThrows = assertThrows(
                RuntimeException.class, () -> {authorizationService.registerAdmin(signupRequest); });
        assertTrue(assertThrows.toString().contains("Error: User with role admin exists already."));
    }

    @Transactional
    @Test
    void when_registerAdminIsPassedRequestWithUserWithAllRoles_thenUserIsSaved(){
        //User with role ROLE_ADMIN is the only user allowed to have multiple roles
        //Arrange
        userRepository.deleteAll();
        User user = new User("admin","admin@novi.nl","password");
        Set<String> roleNames = new HashSet<>();
        Set<Role> expectedRoles = new HashSet<>();
        roleNames.add("admin");
        roleRepository.findByName(ERole.ROLE_ADMIN).ifPresent(role->expectedRoles.add(role));
        roleNames.add("emp");
        roleRepository.findByName(ERole.ROLE_EMPLOYEE).ifPresent(role->expectedRoles.add(role));
        roleNames.add("man");
        roleRepository.findByName(ERole.ROLE_MANAGER).ifPresent(role->expectedRoles.add(role));
        roleNames.add("coworker");
        roleRepository.findByName(ERole.ROLE_COWORKER).ifPresent(role->expectedRoles.add(role));

        //Act
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername(user.getUsername());
        signupRequest.setPassword(user.getPassword());
        signupRequest.setEmail(user.getEmail());
        signupRequest.setRole(roleNames);
        authorizationService.registerAdmin(signupRequest);

        //Assert
        User savedUser = userRepository.findByUsername(user.getUsername()).get();
        String actual = savedUser.getUsername();
        String expected =user.getUsername();
        Set<Role> actualRoles= savedUser.getRoles();

        assertEquals(actual,expected);
        assertTrue(actualRoles.size()==expectedRoles.size());
        assertTrue(actualRoles.containsAll(expectedRoles));
    }

    @Transactional
    @Test
    void when_registerCoworkerIsPassedRequestWithoutRole_thenUserIsSavedWithRoleCoworker(){
        //Arrange
        User user = new User("coworker100","coworker100@novi.nl","password");

        //Act
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername(user.getUsername());
        signupRequest.setPassword(user.getPassword());
        signupRequest.setEmail(user.getEmail());
        authorizationService.registerCoworker(signupRequest);

        //Assert
        User savedUser = userRepository.findByUsername(user.getUsername()).get();
        String actual = savedUser.getUsername();
        String expected =user.getUsername();
        assertEquals(actual,expected);
        assertEquals(savedUser.getRoles().size(), 1);
        assertSame(savedUser.getRoles().iterator().next().getName(), ERole.ROLE_COWORKER);

    }

}
