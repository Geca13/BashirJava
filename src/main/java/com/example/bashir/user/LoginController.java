package com.example.bashir.user;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RestController;


import com.example.bashir.shared.CurrentUser;
import com.example.bashir.user.vm.UserVM;
import com.fasterxml.jackson.annotation.JsonView;

@RestController
public class LoginController {
	
	@PostMapping("/api/1.0/login")
	UserVM handleLogin(@CurrentUser User loggedInUser) {
		
		
		return new UserVM(loggedInUser);
	}
	
	
}
