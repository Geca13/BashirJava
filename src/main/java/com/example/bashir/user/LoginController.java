package com.example.bashir.user;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RestController;


import com.example.bashir.shared.CurrentUser;
import com.fasterxml.jackson.annotation.JsonView;

@RestController
public class LoginController {
	
	@PostMapping("/api/1.0/login")
	@JsonView(Views.Base.class)
	User handleLogin(@CurrentUser User loggedInUser) {
		
		
		return loggedInUser;
	}
	
	
}
