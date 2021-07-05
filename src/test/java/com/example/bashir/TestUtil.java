package com.example.bashir;

import com.example.bashir.hoax.Hoax;
import com.example.bashir.user.User;

public class TestUtil {
	
	public static User createValidUser() {
		User user = new User();
		user.setUsername("test-user");
		user.setDisplayName("test-display");
		user.setPassword("P4ssword");
		user.setImage("profile-image.png");
		return user;
	}
	
	public static User createValidUser(String username) {
		User user = createValidUser();
		user.setUsername(username);
		
		return user;
	}
	
	public static Hoax createValidHoax() {
		Hoax hoax = new Hoax();
		hoax.setContent("test content");
		return hoax;
	}

}
