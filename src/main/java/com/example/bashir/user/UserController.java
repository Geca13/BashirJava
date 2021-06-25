package com.example.bashir.user;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.bashir.error.ApiError;
import com.example.bashir.shared.CurrentUser;
import com.example.bashir.shared.GenericResponse;
import com.example.bashir.user.vm.UserUpdateVM;
import com.example.bashir.user.vm.UserVM;

import com.fasterxml.jackson.annotation.JsonView;


@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/1.0")
public class UserController {
	
	@Autowired
	UserService userService;
	

	@PostMapping("/users")
	GenericResponse createUser(@Valid	@RequestBody User user) {
		
		userService.save(user);
		return new GenericResponse("user saved");
	}
	
	@GetMapping("/users")
	
	Page<UserVM> getUsers(@CurrentUser User loggedInUser, Pageable page) {
		return userService.getUsers(loggedInUser,page).map(UserVM::new);
		
	}
	
	@GetMapping("/users/{username}")
	public UserVM getUserByUsername(@PathVariable String username) {
		User user = userService.getByUsername(username);
		return new UserVM(user);
	}
	
	@PutMapping("/users/{id:[0-9]+}")
	@PreAuthorize("#id == principal.id")
	UserVM updateUser(@PathVariable int id, @RequestBody (required = false) UserUpdateVM userUpdate) {
	  User updated = userService.update(id, userUpdate);
	  return new UserVM(updated);
	}
	
	
	@ExceptionHandler({MethodArgumentNotValidException.class})
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	ApiError handleValidationException(MethodArgumentNotValidException exception, HttpServletRequest request) {
		ApiError apiError = new ApiError(400,"Validation Error", request.getServletPath());
		
		BindingResult result = exception.getBindingResult();
		Map<String, String> validationErrors = new HashMap<>();
		
		for (FieldError fieldError : result.getFieldErrors()) {
			validationErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
		}
		apiError.setValidationErrors(validationErrors);
		
		return apiError;
		
		
	}
}
