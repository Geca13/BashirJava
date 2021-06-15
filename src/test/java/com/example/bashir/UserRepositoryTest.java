package com.example.bashir;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.example.bashir.user.User;
import com.example.bashir.user.UserRepository;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
public class UserRepositoryTest {
	
	@Autowired
	TestEntityManager testEntityManager;
	
	@Autowired
	UserRepository userRepository;
	
	@Test
	public void findByUsername_whenUserExists_returnsUser() {
		
		
		testEntityManager.persist(TestUtil.createValidUser());
		
	   User inDb = userRepository.findByUsername("test-user");
	   assertThat(inDb).isNotNull();
	}
	
	@Test
	public void findByUsername_whenUserDoesNotExist_returnsNull() {
		
		User inDb = userRepository.findByUsername("nonexisting");
		   assertThat(inDb).isNull();
		
	}

}
