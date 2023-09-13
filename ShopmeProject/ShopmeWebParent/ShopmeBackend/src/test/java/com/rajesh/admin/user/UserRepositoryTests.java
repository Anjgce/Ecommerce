package com.rajesh.admin.user;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.Rollback;

import com.rajesh.common.entity.Role;
import com.rajesh.common.entity.User;

@DataJpaTest
@AutoConfigureTestDatabase(replace= Replace.NONE)
@Rollback(false)
public class UserRepositoryTests {

	@Autowired
	private UserRepository repo;
	
	@Autowired
	private TestEntityManager entityManager;	//Provided by spring data jpa for unit testing
	
	//User with one role
	@Test
	public void testCreateNewUserWithOneRole() {
		Role roleAdmin = entityManager.find(Role.class, 1);
		User userAnuj = new User("anujk@gmail.com", "anuj1996","Anuj", "Kumar");
		userAnuj.addRole(roleAdmin);
		User savedUser = repo.save(userAnuj);
		
		assertThat(savedUser.getId()).isGreaterThan(0);
	}
	
	//User with two roles
	@Test
	public void testCreateNewUserWithTwoRoles() {
		User userRajesh = new User("rajesh@gmail.com", "rajesh123", "Rajesh", "Kumar");
		Role roleEditor = new Role(3);
		Role roleAssistant = new Role(5);
		userRajesh.addRole(roleEditor);
		userRajesh.addRole(roleAssistant);
		User savedUser = repo.save(userRajesh);
		
		assertThat(savedUser.getId()).isGreaterThan(0);
	}
	
	//Test list all users
	@Test
	public void testListAllUsers() {
		Iterable<User> listUsers = repo.findAll();
		listUsers.forEach(user -> System.out.println(user));
		
	}
	
	//Test Get user by id
	@Test
	public void testGetUserById() {
		User userAnuj = repo.findById(1).get();
		System.out.println(userAnuj);
		assertThat(userAnuj).isNotNull();
	}
	
	//Test update user details
	@Test
	public void testUpdateUserDetails() {
		User userAnuj = repo.findById(1).get();
		userAnuj.setEnabled(true);
		userAnuj.setEmail("anujgce@gmail.com");
		repo.save(userAnuj);
	}
	
	//Test update user roles
	@Test
	public void testUpdateUserRoles() {
		User userRajesh = repo.findById(2).get();
		Role roleEditor = new Role(3);
		Role roleSalesPerson = new Role(2);
		userRajesh.getRoles().remove(roleEditor);
		userRajesh.addRole(roleSalesPerson);
		repo.save(userRajesh);
	
	}
	
	//Test delete user
	@Test
	public void testDeleteUser() {
		Integer userId = 2;
		repo.deleteById(userId);
	}
	
	//Test getUserByEmail
	@Test
	public void testGetUserByEmail() {
		String email = "ramesh@gmail.com";
		User newEmail = repo.getUserByEmail(email);
		assertThat(newEmail).isNotNull();
	}
	
	//Test countById
	@Test
	public void testCountById() {
		Integer id = 4;
		Long userCount = repo.countById(id);
		assertThat(userCount).isNotNull().isGreaterThan(0);
	}
	
	//Test updateDisableStatus
	@Test
	public void testDisableUser() {
		Integer id = 1;
		repo.updateEnabledStatus(id, false);
	}
	
	//Test Test updateEnableStatus
	@Test
	public void testEnableUser() {
		Integer id = 1;
		repo.updateEnabledStatus(id, true);
	}
	
	@Test
	public void testListFirstPage() {
		int pageNumber = 0;
		int pageSize = 4;
		Pageable pageable = PageRequest.of(pageNumber, pageSize);
		
		Page<User> page = repo.findAll(pageable);
		List<User> listUsers = page.getContent();
		listUsers.forEach(user->System.out.println(user));
		assertThat(listUsers.size()).isEqualTo(pageSize);
	}
	
	@Test
	public void testSearchUsers() {
		String keyword = "bruce";
		int pageNumber = 0;
		int pageSize = 4;
		Pageable pageable = PageRequest.of(pageNumber, pageSize);
		
		Page<User> page = repo.findAll(keyword, pageable);
		List<User> listUsers = page.getContent();
		listUsers.forEach(user->System.out.println(user));
		assertThat(listUsers.size()).isGreaterThan(0);
	}
}
