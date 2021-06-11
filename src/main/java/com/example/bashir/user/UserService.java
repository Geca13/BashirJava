package com.example.bashir.user;


import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.bashir.error.DuplicateUsernameException;

@Service
public class UserService {
	
	//@Autowired
	UserRepository userRepository;
	BCryptPasswordEncoder encoder;

	public UserService(UserRepository userRepository) {
		super();
		this.userRepository = userRepository;
		this.encoder= new BCryptPasswordEncoder();
	}
	
	public User save (User user) {
		User inDb =userRepository.findByUsername(user.getUsername());
		if(inDb != null) {
			throw new DuplicateUsernameException();
		}
		user.setPassword(encoder.encode(user.getPassword()));
		return userRepository.save(user);
		
	}
	
	

}
