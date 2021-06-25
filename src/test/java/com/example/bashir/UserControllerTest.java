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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.example.bashir.error.ApiError;
import com.example.bashir.shared.GenericResponse;
import com.example.bashir.user.User;
import com.example.bashir.user.UserRepository;
import com.example.bashir.user.UserService;
import com.example.bashir.user.vm.UserUpdateVM;
import com.example.bashir.user.vm.UserVM;



@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class UserControllerTest {
	
	private static final String API_1_0_USERS = "/api/1.0/users";
	
	@Autowired
	TestRestTemplate testRestTemplate;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	UserService userService;
	
	@BeforeEach
	public void cleanup() {
		userRepository.deleteAll();
		testRestTemplate.getRestTemplate().getInterceptors().clear();
	}

	@Test
	public void postUser_whenUserIsValid_receiveOk() {
		User user = TestUtil.createValidUser();
		ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	
	@Test
	public void postUser_whenUserIsValid_userSavedToDatabase() {
		User user = TestUtil.createValidUser();
		postSignup(user, Object.class);
		assertThat(userRepository.count()).isEqualTo(1);
	}
	
	@Test
	public void postUser_whenUserIsValid_receiveSuccessMessage() {
		User user = TestUtil.createValidUser();
		ResponseEntity<GenericResponse> response = postSignup(user, GenericResponse.class);
		assertThat(response.getBody().getMessage()).isNotNull();
	}
	
	@Test
	public void postUser_whenUserIsValid_passwordIsHashedInDatabase() {
		User user = TestUtil.createValidUser();
		testRestTemplate.postForEntity(API_1_0_USERS, user, Object.class);
		List<User> users = userRepository.findAll();
		User inDB = users.get(0);
		assertThat(inDB.getPassword()).isNotEqualTo(user.getPassword());
	}
	
	@Test
	public void postUser_whenUserHasNullUsername_receiveBadRequest() {
		User user = TestUtil.createValidUser();
		user.setUsername(null);
		ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	
	@Test
	public void postUser_whenUserHasNullDisplayName_receiveBadRequest() {
		User user = TestUtil.createValidUser();
		user.setDisplayName(null);
		ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	
	@Test
	public void postUser_whenUserHasNullPassword_receiveBadRequest() {
		User user = TestUtil.createValidUser();
		user.setPassword(null);
		ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	
	@Test
	public void postUser_whenUserHasUsernameWithLessThanRequired_receiveBadRequest() {
		User user = TestUtil.createValidUser();
		user.setUsername("abc");
		ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	
	@Test
	public void postUser_whenUserHasDisplayNameWithLessThanRequired_receiveBadRequest() {
		User user = TestUtil.createValidUser();
		user.setDisplayName("abc");
		ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	
	@Test
	public void postUser_whenUserHasPasswordWithLessThanRequired_receiveBadRequest() {
		User user = TestUtil.createValidUser();
		user.setPassword("P4sswd");
		ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	
	@Test
	public void postUser_whenUserHasUsernameExceedsTheLengthLimit_receiveBadRequest() {
		User user = TestUtil.createValidUser();
		String valueOf256Chars = IntStream.rangeClosed(1,256).mapToObj(x -> "a").collect(Collectors.joining());
		user.setUsername(valueOf256Chars);
		ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	
	@Test
	public void postUser_whenUserHasDisplayNameExceedsTheLengthLimit_receiveBadRequest() {
		User user = TestUtil.createValidUser();
		String valueOf256Chars = IntStream.rangeClosed(1,256).mapToObj(x -> "a").collect(Collectors.joining());
		user.setDisplayName(valueOf256Chars);
		ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	
	@Test
	public void postUser_whenUserHasPasswordExceedsTheLengthLimit_receiveBadRequest() {
		User user = TestUtil.createValidUser();
		String valueOf256Chars = IntStream.rangeClosed(1,256).mapToObj(x -> "a").collect(Collectors.joining());
		user.setPassword(valueOf256Chars + "A1");
		ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	
	@Test
	public void postUser_whenUserHasPasswordWithAllLowercase_receiveBadRequest() {
		User user = TestUtil.createValidUser();
		user.setPassword("alllowercase");
		ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	
	@Test
	public void postUser_whenUserHasPasswordWithAllUppercase_receiveBadRequest() {
		User user = TestUtil.createValidUser();
		user.setPassword("ALLUPPERCASE");
		ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	
	@Test
	public void postUser_whenUserHasPasswordWithAllNumber_receiveBadRequest() {
		User user = TestUtil.createValidUser();
		user.setPassword("123456789");
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
	public void postUser_whenUserHasNullUsername_receiveMessageOfNullErrorForUsername() {
		User user = TestUtil.createValidUser();
		user.setUsername(null);
		ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
		Map<String, String> validationErrors = response.getBody().getValidationErrors();
		assertThat(validationErrors.get("username")).isEqualTo("Username cannot be null");
	}
	
	@Test
	public void postUser_whenUserHasNullPassword_receiveGenericMessageOfNullError() {
		User user = TestUtil.createValidUser();
		user.setPassword(null);
		ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
		Map<String, String> validationErrors = response.getBody().getValidationErrors();
		assertThat(validationErrors.get("password")).isEqualTo("Cannot be null");
	}
	
	@Test
	public void postUser_whenUserHasInvalidLengthUsername_receiveGenericMessageOfSizeError() {
		User user = TestUtil.createValidUser();
		user.setUsername("abc");
		ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
		Map<String, String> validationErrors = response.getBody().getValidationErrors();
		assertThat(validationErrors.get("username")).isEqualTo("It must have minimum 4 and maximum 255 characters");
	}
	
	@Test
	public void postUser_whenUserHasInvalidPasswordPattern_receiveMessageOfPasswordPatternError() {
		User user = TestUtil.createValidUser();
		user.setPassword("alllowercase");
		ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
		Map<String, String> validationErrors = response.getBody().getValidationErrors();
		assertThat(validationErrors.get("password")).isEqualTo("Password must have at least one uppercase, one lowercase letter and one number");
	}
	
	@Test
	public void postUser_whenAnotherUserHasSameUsername_receiveBadRequest() {
		userRepository.save(TestUtil.createValidUser());
		
		User user = TestUtil.createValidUser();
		ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	
	@Test
	public void postUser_whenAnotherUserHasSameUsername_receiveMessageOfDuplicateUsernamet() {
		userRepository.save(TestUtil.createValidUser());
		
		User user = TestUtil.createValidUser();
		ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
		Map<String, String> validationErrors = response.getBody().getValidationErrors();
		assertThat(validationErrors.get("username")).isEqualTo("This name is in use");
	}
	
	@Test
	public void getUsers_whenThereAreNoUsersInDb_receive200Ok() {
		ResponseEntity<Object> response = getUsers(new ParameterizedTypeReference<Object>() { });
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}
	
	@Test
	public void getUsers_whenThereAreNoUsersInDb_receivePageWithZeroItems() {
		ResponseEntity<TestPage<Object>> response = getUsers(new ParameterizedTypeReference<TestPage<Object>>() { });
		assertThat(response.getBody().getTotalElements()).isEqualTo(0);
	}
	
	@Test
	public void getUsers_whenThereIsUsersInDb_receivePageWithUser() {
		userRepository.save(TestUtil.createValidUser());
		ResponseEntity<TestPage<Object>> response = getUsers(new ParameterizedTypeReference<TestPage<Object>>() { });
		assertThat(response.getBody().getNumberOfElements()).isEqualTo(1);
	}
	
	@Test
	public void getUsers_whenThereIsUsersInDb_receiveUserWithoutPassword() {
		userRepository.save(TestUtil.createValidUser());
		ResponseEntity<TestPage<Map<String , Object>>> response = getUsers(new ParameterizedTypeReference<TestPage<Map<String , Object>>>() { });
		Map<String , Object> entity = response.getBody().getContent().get(0);
		assertThat(entity.containsKey("password")).isFalse();
	}
	
	@Test
	public void getUsers_whenPageIsRequestedFor3UsersPerPageWhenTheDBhas20Users_receive3Users() {
		IntStream.rangeClosed(1, 20).mapToObj(i -> "test-user"+i).map(TestUtil::createValidUser).forEach(userRepository::save);
		String path = API_1_0_USERS + "?page=0&size=3";
		ResponseEntity<TestPage<Object>> response = getUsers(path, new ParameterizedTypeReference<TestPage<Object>>() {});
		assertThat(response.getBody().getContent().size()).isEqualTo(3);
		
	}
	
	@Test
	public void getUsers_whenPageSizeNotProvided_receivePageSizeAs10() {
		ResponseEntity<TestPage<Object>> response = getUsers(new ParameterizedTypeReference<TestPage<Object>>() { });
		assertThat(response.getBody().getSize()).isEqualTo(10);
	}
	
	@Test
	public void getUsers_whenPageSizeIsGreaterThen100_receivePageSizeAs100() {
		String path = API_1_0_USERS + "?size=500";

		ResponseEntity<TestPage<Object>> response = getUsers(path ,new ParameterizedTypeReference<TestPage<Object>>() { });
		
		assertThat(response.getBody().getSize()).isEqualTo(100);
	}
	
	@Test
	public void getUsers_whenPageSizeIsNegative_receivePageSizeAs10() {
		String path = API_1_0_USERS + "?size=-5";

		ResponseEntity<TestPage<Object>> response = getUsers(path ,new ParameterizedTypeReference<TestPage<Object>>() { });
		
		assertThat(response.getBody().getSize()).isEqualTo(10);
	}
	
	@Test
	public void getUsers_whenPageIsNegative_receiveFirstPage() {
		String path = API_1_0_USERS + "?page=-5";

		ResponseEntity<TestPage<Object>> response = getUsers(path ,new ParameterizedTypeReference<TestPage<Object>>() { });
		
		assertThat(response.getBody().getNumber()).isEqualTo(0);
	}
	
	@Test
	public void getUsers_whenUserLoggedIn_receivePageWithoutLoggedInUser() {
		userService.save(TestUtil.createValidUser("user1"));
		userService.save(TestUtil.createValidUser("user2"));
		userService.save(TestUtil.createValidUser("user3"));

        authenticate("user1");
		ResponseEntity<TestPage<Object>> response = getUsers(new ParameterizedTypeReference<TestPage<Object>>() { });
		
		assertThat(response.getBody().getTotalElements()).isEqualTo(2);
	}
	
	@Test
	public void getUserByUsername_whenUserExist_receiveOk() {
		
		String username = "test-user";
		userService.save(TestUtil.createValidUser(username));
		ResponseEntity<Object> response = getUser(username, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}
	
	@Test
	public void getUserByUsername_whenUserExist_receiveUserWithoutPassword() {
		
		String username = "test-user";
		userService.save(TestUtil.createValidUser(username));
		ResponseEntity<String> response = getUser(username, String.class);
		assertThat(response.getBody().contains("password")).isFalse();
	}
	
	@Test
	public void getUserByUsername_whenUserDoesNotExist_receiveUserNotFound() {
		
		ResponseEntity<Object> response = getUser("Unknown-user", Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}
	
	@Test
	public void getUserByUsername_whenUserDoesNotExist_receiveApiError() {
		
		ResponseEntity<ApiError> response = getUser("Unknown-user", ApiError.class);
		assertThat(response.getBody().getMessage().contains("unknown-use")).isTrue();
	}
	
	@Test
	public void putUser_whenAuthorizedUserSendsUpdateForOtherUser_receiveForbidden() {
		
		ResponseEntity<Object> response = putUser(123,  null, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}
	
	@Test
	public void putUser_whenUserIsUnauthorized_receiveUnauthorized() {
		User user =userService.save(TestUtil.createValidUser("user1"));
		authenticate(user.getUsername());
		int differentUser = user.getId()+123;
		ResponseEntity<Object> response = putUser(differentUser,  null, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}
	
	@Test
	public void putUser_whenAuthorizedUserSendsUpdateForOtherUser_receiveApiError() {
		
		ResponseEntity<ApiError> response = putUser(123,  null, ApiError.class);
		assertThat(response.getBody().getUrl().contains("user/123"));
	}
	
	@Test
	public void putUser_whenValidRequestBodyFromAutherizedUser_receiveOk() {
		User user =userService.save(TestUtil.createValidUser("user1"));
		authenticate(user.getUsername());
		UserUpdateVM updateVM = createValidUserUpdateVM();
		HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updateVM);
		ResponseEntity<Object> response = putUser(user.getId(),  requestEntity, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}
	
	@Test
	public void putUser_whenValidRequestBodyFromAutherizedUser_displayNameUpdated() {
		User user =userService.save(TestUtil.createValidUser("user1"));
		authenticate(user.getUsername());
		UserUpdateVM updateVM = createValidUserUpdateVM();
		HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updateVM);
		putUser(user.getId(),  requestEntity, Object.class);
		User userInDb = userRepository.findByUsername("user1");
		assertThat(userInDb.getDisplayName()).isEqualTo(updateVM.getDisplayName());
	}
	
	@Test
	public void putUser_whenValidRequestBodyFromAutherizedUser_receiveUserVmWithUpdatedDisplayName() {
		User user =userService.save(TestUtil.createValidUser("user1"));
		authenticate(user.getUsername());
		UserUpdateVM updateVM = createValidUserUpdateVM();
		HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updateVM);
		ResponseEntity<UserVM> response = putUser(user.getId(),  requestEntity, UserVM.class);
		
		assertThat(response.getBody().getDisplayName()).isEqualTo(updateVM.getDisplayName());
	}

	private UserUpdateVM createValidUserUpdateVM() {
		UserUpdateVM updateVM = new UserUpdateVM();
		updateVM.setDisplayName("newDisplayName");
		return updateVM;
	}
	
	@Test
	public void putUser_whenUserIsUnauthorized_receiveApiError() {
		User user =userService.save(TestUtil.createValidUser("user1"));
		authenticate(user.getUsername());
		int differentUser = user.getId()+123;
		ResponseEntity<ApiError> response = putUser(differentUser,  null, ApiError.class);
		assertThat(response.getBody().getUrl().contains("users/"+differentUser));
	}
	
	private void authenticate(String username) {
		testRestTemplate.getRestTemplate().getInterceptors().add(new BasicAuthenticationInterceptor(username, "P4ssword"));
	}
	
	public <T> ResponseEntity<T> postSignup(Object request , Class<T>response){
		return testRestTemplate.postForEntity(API_1_0_USERS, request, response);
	}
	
	public <T> ResponseEntity<T> getUsers(ParameterizedTypeReference<T>responseType){
		return testRestTemplate.exchange(API_1_0_USERS,HttpMethod.GET, null ,responseType);
	}
	
	public <T> ResponseEntity<T> getUsers(String path ,ParameterizedTypeReference<T>responseType){
		return testRestTemplate.exchange(path,HttpMethod.GET, null ,responseType);
	}
	
	public <T> ResponseEntity<T> getUser(String username, Class<T> responseType){
		String path = API_1_0_USERS + "/"+username;
		return testRestTemplate.getForEntity(path, responseType);
	}
	
	public <T> ResponseEntity<T>putUser(long id,HttpEntity<?> requestEntity , Class<T> responseType){
		String path = API_1_0_USERS + "/"+ id;
		return testRestTemplate.exchange(path,HttpMethod.PUT, requestEntity, responseType);
	}
	
	
	
	
	

}
