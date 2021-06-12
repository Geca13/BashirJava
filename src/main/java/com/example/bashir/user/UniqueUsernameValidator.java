package com.example.bashir.user;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

public class UniqueUsernameValidator implements ConstraintValidator<UniqueUsername, String>  {
	
	@Autowired
	UserRepository userRepository;
	

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		
		User inDb = userRepository.findByUsername(value);
		if(inDb == null) {
			return true;
		}
		
		return false;
	}

}
