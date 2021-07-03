package com.example.bashir.user;


import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.bashir.error.NotFound;
import com.example.bashir.user.vm.UserUpdateVM;



@Service
public class UserService {
	
	//@Autowired
	UserRepository userRepository;
	PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository,PasswordEncoder passwordEncoder) {
		super();
		this.userRepository = userRepository;
		this.passwordEncoder= passwordEncoder;
	}
	
	public User save (User user) {
		
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		return userRepository.save(user);
		
	}

	public Page<User> getUsers(User loggedInUser, Pageable pageable) {
		if(loggedInUser != null) {
			return userRepository.findByUsernameNot(loggedInUser.getUsername(),pageable);
		}
		return userRepository.findAll(pageable);
	}

	public User getByUsername(String username) {
		
		User userInDb= userRepository.findByUsername(username);
		if(userInDb == null) {
			throw new NotFound(username + " not found");
		}
		
		return userInDb;
		
	}

	public User update(int id, UserUpdateVM userUpdate) {
		
		User inDb = userRepository.findById(id).get();
		inDb.setDisplayName(userUpdate.getDisplayName());
		String savedImageName = inDb.getUsername() + UUID.randomUUID().toString().replaceAll("-", "");
		inDb.setImage(savedImageName);
		return userRepository.save(inDb);
	}
	
	

}
