package com.example.bashir.user;


import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

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
		user.setPassword(encoder.encode(user.getPassword()));
		return userRepository.save(user);
		
	}
	
	

}
