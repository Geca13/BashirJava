package com.example.bashir.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bashir.shared.GenericResponse;

@RestController
public class UserController {
	
	@Autowired
	UserService userService;
	

	@PostMapping("/api/1.0/users")
	GenericResponse createUser(@RequestBody User user) {
		
		userService.save(user);
		return new GenericResponse("user saved");
	}
}
