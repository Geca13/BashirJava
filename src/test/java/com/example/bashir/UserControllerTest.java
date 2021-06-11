package com.example.bashir;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.example.bashir.error.ApiError;
import com.example.bashir.shared.GenericResponse;
import com.example.bashir.user.User;
import com.example.bashir.user.UserRepository;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class UserControllerTest {
	
	private static final String API_1_0_USERS = "/api/1.0/users";
	
	@Autowired
	TestRestTemplate testRestTemplate;
	
	@Autowired
	UserRepository userRepository;
	
	
	@BeforeEach
	public void cleanup() {
		userRepository.deleteAll();
	}
	
	@Test
	public void postUser_whenUserIsValid_receiveOk() {
		User user = createValidUser();
		ResponseEntity<Object> response = postSignup( user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	
	
	@Test
	public void postUser_whenUserIsValid_userSavedToDatabase() {
		
		User user = createValidUser();
		postSignup( user, Object.class);
		assertThat(userRepository.count()).isEqualTo(1);
		
		
	}
	
	@Test
	public void postUser_whenUserIsValid_receiveSuccess() {
		User user = createValidUser();
		ResponseEntity<GenericResponse> response = postSignup( user, GenericResponse.class);
		assertThat(response.getBody().getMessage()).isNotNull();
	}
	
	@Test
	public void postUser_whenUserIsValid_passwordIsHashedInDatabase() {
		
		User user = createValidUser();
		testRestTemplate.postForEntity(API_1_0_USERS, user, Object.class);
		List<User> users = userRepository.findAll();
		User inDb = users.get(0);
		assertThat(inDb.getPassword()).isNotEqualTo(user.getPassword());
		
	}
	
	@Test
	public void postUser_whenUserHasNullUsername_receiveBadRequest() {
		User user = createValidUser();
		user.setUsername(null);
		ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	
	@Test
	public void postUser_whenUserHasNullDisplayName_receiveBadRequest() {
		User user = createValidUser();
		user.setDisplayName(null);
		ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	
	@Test
	public void postUser_whenUserHasNullPassword_receiveBadRequest() {
		User user = createValidUser();
		user.setPassword(null);
		ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	
	@Test
	public void postUser_whenUserHasUsernameWithLessThenRequired_receiveBadRequest() {
		User user = createValidUser();
		user.setUsername("abc");
		ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	
	@Test
	public void postUser_whenUserHasDisplayNameWithLessThenRequired_receiveBadRequest() {
		User user = createValidUser();
		user.setDisplayName("abc");
		ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	
	@Test
	public void postUser_whenUserHasPasswordWithLessThenRequired_receiveBadRequest() {
		User user = createValidUser();
		user.setPassword("P4ss");
		ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	
	@Test
	public void postUser_whenUserHasUsernameExceedsTheMaxLenght_receiveBadRequest() {
		User user = createValidUser();
		String maxValueOf256Chars = IntStream.rangeClosed(1, 256).mapToObj(x -> "a").collect(Collectors.joining());
		user.setUsername(maxValueOf256Chars);
		ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	
	@Test
	public void postUser_whenUserHasDisplayNameExceedsTheMaxLenght_receiveBadRequest() {
		User user = createValidUser();
		String maxValueOf256Chars = IntStream.rangeClosed(1, 256).mapToObj(x -> "a").collect(Collectors.joining());
		user.setDisplayName(maxValueOf256Chars);
		ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	
	@Test
	public void postUser_whenUserPasswordExceedsTheMaxLenght_receiveBadRequest() {
		User user = createValidUser();
		String maxValueOf256Chars = IntStream.rangeClosed(1, 256).mapToObj(x -> "a").collect(Collectors.joining());
		user.setPassword(maxValueOf256Chars + "A1");
		ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	
	@Test
	public void postUser_whenUserPasswordWithAllLowerCase_receiveBadRequest() {
		User user = createValidUser();
		user.setPassword("alllowercase");
		ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	
	@Test
	public void postUser_whenUserPasswordWithAllUpperCase_receiveBadRequest() {
		User user = createValidUser();
		user.setPassword("ALLUPPERCASE");
		ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	
	@Test
	public void postUser_whenUserPasswordWithAllNumbersCase_receiveBadRequest() {
		User user = createValidUser();
		user.setPassword("111223344556");
		ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	
	@Test
	public void postUser_whenUserIsInvalid_receiveApiError() {
		User user = new User();
		ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
		assertThat(response.getBody().getUrl()).isEqualTo(API_1_0_USERS);
		}
	
	@Test
	public void postUser_whenUserIsInvalid_receiveApiErrorWithValidationErrors() {
		User user = new User();
		ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
		assertThat(response.getBody().getValidationErrors().size()).isEqualTo(3);
		}
	
	@Test
	public void postUser_whenUserHasNullUsername_receiveErrorOfNullErrorUsername() {
		User user = createValidUser();
		user.setUsername(null);
		ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
        Map<String, String> validationErrors = response.getBody().getValidationErrors();
        assertThat(validationErrors.get("username")).isEqualTo("Username cannot be null");
		
	}
	
	@Test
	public void postUser_whenUserHasNullPassword_receiveErrorOfNullErrorUsername() {
		User user = createValidUser();
		user.setPassword(null);
		ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
        Map<String, String> validationErrors = response.getBody().getValidationErrors();
        assertThat(validationErrors.get("password")).isEqualTo("Cannot be null");
		
	}
	
	@Test
	public void postUser_whenUserHasInvalidUsernameLength_receiveGenericMessageOfSizeError() {
		User user = createValidUser();
		user.setUsername("abc");
		ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
        Map<String, String> validationErrors = response.getBody().getValidationErrors();
        assertThat(validationErrors.get("username")).isEqualTo("It must have minimum 4 and maximum 255 char");
		
	}
	
	@Test
	public void postUser_whenUserHasInvalidPasswordPattern_receiveMessageOfPasswordPatternError() {
		User user = createValidUser();
		user.setPassword("alllowercase");
		ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
        Map<String, String> validationErrors = response.getBody().getValidationErrors();
        assertThat(validationErrors.get("password")).isEqualTo("Password must have at least one uppercase, one lowercase letter and one number");
		
	}
	
	@Test
	public void postUser_whenAnotherUserHasTheSameUsername_receiveBadRequest() {
		userRepository.save(createValidUser());
		
		User user = createValidUser();
		ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	
	public <T> ResponseEntity<T> postSignup(Object request , Class<T>response){
		return testRestTemplate.postForEntity(API_1_0_USERS, request, response);
	}
	
	private User createValidUser() {
		User user = new User();
		user.setUsername("test-user");
		user.setDisplayName("test-display");
		user.setPassword("P4ssword");
		return user;
	}
	
	

}
