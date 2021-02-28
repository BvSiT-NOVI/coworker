package nl.bvsit.coworker.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.bvsit.coworker.domain.ERole;
import nl.bvsit.coworker.domain.Role;
import nl.bvsit.coworker.domain.User;
import nl.bvsit.coworker.repository.RoleRepository;
import nl.bvsit.coworker.repository.UserRepository;
import nl.bvsit.coworker.service.AuthorizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class UserDataConfig implements CommandLineRunner {
    private static final Logger logger =  LoggerFactory.getLogger(UserDataConfig.class);
    final static boolean VERBOSE=false;

    @Autowired
    AuthorizationService authorizationService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        logger.info("Running " + this.getClass().getName());

        createRoles(true);

        if (!CwConstants.CREATE_TEST_USERS) return;

        if (CwConstants.LOAD_MINIMAL_TEST_DATA){
            createUser(new User("admin", "admin@novi.nl", "password"),true, ERole.ROLE_ADMIN);
            createUser(new User("emp1", "emp@novi.nl", "password"),true, ERole.ROLE_EMPLOYEE);
            createUser(new User("coworker1", "cow@novi.nl", "password"), true,ERole.ROLE_COWORKER);
            createUser(new User("coworker2", "cow2@novi.nl", "password"), true,ERole.ROLE_COWORKER);
            createUser(new User("coworker3", "cow3@novi.nl", "password"), true,ERole.ROLE_COWORKER);
            createUser(new User("man1", "man@novi.nl", "password"), true, ERole.ROLE_MANAGER,ERole.ROLE_EMPLOYEE);
        }
        else {
            createUser(new User("admin", "admin@novi.nl", "password"), ERole.ROLE_ADMIN);
            createUser(new User("admin2", "admin2@novi.nl", "password"), ERole.ROLE_ADMIN, ERole.ROLE_MANAGER);
            createUser(new User("emp", "emp@novi.nl", "password"), ERole.ROLE_EMPLOYEE);
            createUser(new User("user3", "user3@novi.nl", "password"), ERole.ROLE_COWORKER, ERole.ROLE_ADMIN);
            //createUser(new User("user3", "user3@novi.nl", "password"), ERole.ROLE_COWORKER);
            createUser(new User("user4", "user4@novi.nl", "password"), ERole.ROLE_ADMIN);
            for (int i = 0; i < 5; i++) {
                createUser(createUser(new User("user", "user@novi.nl", "password"), i + 5, ERole.ROLE_COWORKER));
            }
        }
    }

    public void createRoles(boolean verbose){
        roleRepository.save(new Role(ERole.ROLE_ADMIN));
        roleRepository.save(new Role(ERole.ROLE_MANAGER));
        roleRepository.save(new Role(ERole.ROLE_EMPLOYEE));
        roleRepository.save(new Role(ERole.ROLE_COWORKER));
        if (verbose) {
            logger.info("Created and saved Roles:");
            roleRepository.findAll().forEach(role ->LogUtil.logMappedObject(role,logger));
        }
    }

    public User createUser(User user, ERole... roles) {
        return createUser(user,VERBOSE, roles);
    }

    public User createUser(User user,boolean verbose, ERole... roles){

        for(ERole role:roles ){
            if (roleRepository.existsByName(role)) {
                Role foundRole = roleRepository.findByName(role).orElse(null);
                if (foundRole!=null) user.addRole(foundRole);
            }
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        try {user = userRepository.save(user);} catch (DataAccessException e) {e.printStackTrace();}
        if (verbose){
            userRepository.findByNameFetchAll(user.getUsername())
                    .ifPresent(
                            newUser
                                    -> {logger.info("User created:");
                                LogUtil.logMappedObject(newUser,logger);});
        }
        return user;
    }

    public User createUser(User user,int idx ,ERole... roles){
        user.setUsername(user.getUsername()+idx);
        user.setEmail( user.getEmail().replace("@",idx+"@"));
        return createUser(user,roles);
    }

}

