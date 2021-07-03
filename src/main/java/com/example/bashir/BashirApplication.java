package com.example.bashir;

import java.util.stream.IntStream;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import com.example.bashir.user.User;
import com.example.bashir.user.UserService;

@SpringBootApplication
@EnableConfigurationProperties 
public class BashirApplication {

	public static void main(String[] args) {
		SpringApplication.run(BashirApplication.class, args);
	}
	
	@Bean
	@Profile("dev")
	CommandLineRunner run(UserService service) {
		return (args) ->{
				IntStream.rangeClosed(1, 15).mapToObj(i ->{
					User user = new User();
					user.setUsername("user"+i);
					user.setDisplayName("user"+i);
					user.setPassword("P4ssword");
					return user;
				}).forEach(service:: save);
			
			
		};
		
	}

}
