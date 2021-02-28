package nl.bvsit.coworker.repository;

import nl.bvsit.coworker.domain.ERole;
import nl.bvsit.coworker.domain.Role;
import nl.bvsit.coworker.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@Transactional //Important! Reverts all changes to the database
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) //use Postgres DB
class UserRepositoryIntegrationTest {
	private static final Logger logger =  LoggerFactory.getLogger(UserRepositoryIntegrationTest.class);
	@Autowired
	UserRepository userRepository;
	@Autowired
	RoleRepository roleRepository;

	@BeforeEach
	void init(){}

	@Transactional
	@Test
	void test_findFirstByRolesContains(){
		//Arrange
		//Use exclusively default repository methods to delete all admin users
		deleteAllUsersWithRole(ERole.ROLE_ADMIN);

		//save admin user
		Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN).orElse(null);
		assertNotNull(adminRole);
		User user = new User("admin","admin@novi.nl","password");
		user.addRole(adminRole);
		User expected = userRepository.save(user);

		//Act
		User actual = userRepository.findFirstByRolesContains(adminRole).orElse(null);

		//Assert
		assertEquals(expected,actual);
	}

	@Transactional
	@Test
	void test_findByRole(){
		//Arrange

		//Use exclusively default repository methods to delete all admin users
		deleteAllUsersWithRole(ERole.ROLE_ADMIN);

		//Create 2 admin users and add to list
		Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN).orElse(null);
		assertNotNull(adminRole);
		List<User> expectedUsers = new ArrayList<>();
		User user = new User("admin","admin@novi.nl","password");
		user.addRole(adminRole);
		user = userRepository.save(user);
		expectedUsers.add(user);
		user = new User("admin2","admin2@novi.nl","password");
		user.addRole(adminRole);
		user = userRepository.save(user);
		expectedUsers.add(user);
		expectedUsers.sort(Comparator.comparing(User::getUsername));

		//Act
		//Use custom method to find admin users
		List<User> actualUsers = userRepository.findByRole(ERole.ROLE_ADMIN);
		actualUsers.sort(Comparator.comparing(User::getUsername));
		actualUsers.forEach(u-> System.out.println(u.getUsername()));

		//Assert
		assertEquals(expectedUsers,actualUsers);
	}

	@Transactional
	@Test
	void test_existsByRole(){
		//Arrange

		//Use exclusively default repository methods to delete all admin users
		deleteAllUsersWithRole(ERole.ROLE_ADMIN);

		//List<User> list = userRepository.findAll();

		//save admin user
		Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN).orElse(null);
		assertNotNull(adminRole);
		User user = new User("admin","admin@novi.nl","password");
		user.addRole(adminRole);
		userRepository.save(user);

		//Act
		boolean actual = userRepository.existsByRole(ERole.ROLE_ADMIN);
		boolean expected = true;
		//Assert
		assertEquals(actual,expected);
	}


	void deleteAllUsersWithRole(ERole roleName){
		//delete all users with a role using only default repository methods
		List<User> usersToDelete = new ArrayList<>();
		for(User user:userRepository.findAll()){
			for(Role role : user.getRoles()){
				if (role.getName()==roleName){
					usersToDelete.add(user);
					break;
				}
			}
		}
		userRepository.deleteAll(usersToDelete);
		userRepository.flush(); //Without flush synchronization errors could occur
	}

}
