package com.example.bashir.user;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.bashir.error.ApiError;

@RestController
public class LoginController {
	
	@PostMapping("/api/1.0/login")
	void handleLogin() {
		
	}
	
	
}
