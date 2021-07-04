package com.example.bashir.user;


import java.io.IOException;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.bashir.error.NotFound;
import com.example.bashir.file.FileService;
import com.example.bashir.user.vm.UserUpdateVM;



@Service
public class UserService {
	
	//@Autowired
	UserRepository userRepository;
	PasswordEncoder passwordEncoder;
	FileService fileService;

	public UserService(UserRepository userRepository,PasswordEncoder passwordEncoder,FileService fileService) {
		super();
		this.userRepository = userRepository;
		this.passwordEncoder= passwordEncoder;
		this.fileService= fileService;
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
		if(userUpdate.getImage() != null) {
		String savedImageName;
		try {
			savedImageName = fileService.saveProfileImage(userUpdate.getImage());
			fileService.deleteProfileImage(inDb.getImage());
			inDb.setImage(savedImageName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		return userRepository.save(inDb);
	}
	
	

}
