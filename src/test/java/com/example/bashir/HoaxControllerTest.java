package com.example.bashir;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import com.example.bashir.configuration.AppConfiguration;
import com.example.bashir.error.ApiError;
import com.example.bashir.file.FileAttachment;
import com.example.bashir.file.FileAttachmentRepository;
import com.example.bashir.file.FileService;
import com.example.bashir.hoax.Hoax;
import com.example.bashir.hoax.HoaxRepository;
import com.example.bashir.hoax.HoaxService;
import com.example.bashir.hoax.HoaxVm;
import com.example.bashir.shared.GenericResponse;
import com.example.bashir.user.User;
import com.example.bashir.user.UserRepository;
import com.example.bashir.user.UserService;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class HoaxControllerTest {
	
	private static final String API_1_0_HOAXES = "/api/1.0/hoaxes";

	@Autowired
	TestRestTemplate testRestTemplate;
	
	@Autowired
	UserService userService;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	HoaxRepository hoaxRepository;
	
	@Autowired
	HoaxService hoaxService;
	
	@Autowired
	FileAttachmentRepository fileAttachmentRepository;
	
	@Autowired
	FileService fileService;
	
	@Autowired
	AppConfiguration appConfiguration;
	
	@PersistenceUnit
	private EntityManagerFactory factory;
	
	@BeforeEach
	public void cleanup() throws IOException {
		fileAttachmentRepository.deleteAll();
		hoaxRepository.deleteAll();
		userRepository.deleteAll();
		testRestTemplate.getRestTemplate().getInterceptors().clear();
		FileUtils.cleanDirectory(new File(appConfiguration.getFullAttachmentsPath()));
	}
	
	private void authenticate(String username) {
		testRestTemplate.getRestTemplate().getInterceptors().add(new BasicAuthenticationInterceptor(username, "P4ssword"));
	}
	
	@Test
	public void postHoax_whenUserIsAuthorized_receiveOk() {
		userService.save(TestUtil.createValidUser("user1"));
		authenticate("user1");
		Hoax hoax = TestUtil.createValidHoax();
		ResponseEntity<Object> response = postHoax( hoax, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}
	
	@Test
	public void postHoax_whenUserIsNotAuthorized_receiveUnauthorized() {
		
		Hoax hoax = TestUtil.createValidHoax();
		ResponseEntity<Object> response = postHoax( hoax, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}
	
	@Test
	public void postHoax_whenUserIsNotAuthorized_receiveApiError() {
		
		Hoax hoax = TestUtil.createValidHoax();
		ResponseEntity<ApiError> response = postHoax( hoax, ApiError.class);
		assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
	}
	
	@Test
	public void postHoax_whenUserIsAuthorized_savedToDatabase() {
		userService.save(TestUtil.createValidUser("user1"));
		authenticate("user1");
		Hoax hoax = TestUtil.createValidHoax();
	    postHoax( hoax, Object.class);
		assertThat(hoaxRepository.count()).isEqualTo(1);
	}
	
	@Test
	public void postHoax_whenUserIsAuthorized_savedToDatabaseWithTimeStamp() {
		userService.save(TestUtil.createValidUser("user1"));
		authenticate("user1");
		Hoax hoax = TestUtil.createValidHoax();
	    postHoax( hoax, Object.class);
	    
	    Hoax inDb = hoaxRepository.findAll().get(0);
		assertThat(inDb.getTimestamp()).isNotNull();
	}
	
	@Test
	public void postHoax_whenUserIsAuthorizedAndHoaxContentIsNull_receiveBadRequest() {
		userService.save(TestUtil.createValidUser("user1"));
		authenticate("user1");
		Hoax hoax = new Hoax();
		
		ResponseEntity<Object> response = postHoax( hoax, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	
	@Test
	public void postHoax_whenUserIsAuthorizedAndHoaxContentIsLessThen10Char_receiveBadRequest() {
		userService.save(TestUtil.createValidUser("user1"));
		authenticate("user1");
		Hoax hoax = new Hoax();
		hoax.setContent("abc");
		ResponseEntity<Object> response = postHoax( hoax, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	
	@Test
	public void postHoax_whenUserIsAuthorizedAndHoaxContentHasValidNumberOfCharacters_receiveOk() {
		userService.save(TestUtil.createValidUser("user1"));
		authenticate("user1");
		Hoax hoax = new Hoax();
		String valueOf5000Chars = IntStream.rangeClosed(1,5000).mapToObj(x -> "a").collect(Collectors.joining());
		hoax.setContent(valueOf5000Chars);
		ResponseEntity<Object> response = postHoax( hoax, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}
	
	@Test
	public void postHoax_whenUserIsAuthorizedAndHoaxContentIsMoreThen5000Char_receiveBadRequest() {
		userService.save(TestUtil.createValidUser("user1"));
		authenticate("user1");
		Hoax hoax = new Hoax();
		String valueOf5001Chars = IntStream.rangeClosed(1,5001).mapToObj(x -> "a").collect(Collectors.joining());
		hoax.setContent(valueOf5001Chars);
		ResponseEntity<Object> response = postHoax( hoax, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	
	@Test
	public void postHoax_whenUserIsAuthorizedAndHoaxContentIsNull_receiveApiErrorWithValidationErrors() {
		userService.save(TestUtil.createValidUser("user1"));
		authenticate("user1");
		Hoax hoax = new Hoax();
		
		ResponseEntity<ApiError> response = postHoax( hoax, ApiError.class);
		Map<String, String> validationErrors = response.getBody().getValidationErrors();
		assertThat(validationErrors.get("content")).isNotNull();
	}
	
	@Test
	public void postHoax_whenUserIsAuthorized_hoaxSavedToDatabaseWithAuthenticatedUserInfo() {
		userService.save(TestUtil.createValidUser("user1"));
		authenticate("user1");
		Hoax hoax = TestUtil.createValidHoax();
	    postHoax( hoax, Object.class);
	    
	    Hoax inDb = hoaxRepository.findAll().get(0);
	    
		assertThat(inDb.getUser().getUsername()).isEqualTo("user1");
	}
	
	
	
	@Test
	public void postHoax_whenUserIsAuthorized_hoaxCanBeAccessedFromUserEntity() {
		User user = userService.save(TestUtil.createValidUser("user1"));
		authenticate("user1");
		Hoax hoax = TestUtil.createValidHoax();
	    postHoax( hoax, Object.class);
	    
	    EntityManager manager = factory.createEntityManager();
	    
	    User inDb = manager.find(User.class, user.getId());
	    
	    
		assertThat(inDb.getHoaxes().size()).isEqualTo(1);
	}
	
	@Test
	public void getHoaxes_whenThereAreNoHoaxes_returnOk() {
		ResponseEntity<Object> response = getHoaxes(new ParameterizedTypeReference<Object>() {}) ;
		
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	public void getHoaxes_whenThereAreNoHoaxes_returnPageWithZeroItems() {
		ResponseEntity<TestPage<Object>> response = getHoaxes(new ParameterizedTypeReference<TestPage<Object>>() {}) ;
		
		assertThat(response.getBody().getTotalElements()).isEqualTo(0);
	}
	
	@Test
	public void getHoaxes_whenThereAreHoaxes_returnPageWithItems() {
		User user = userService.save(TestUtil.createValidUser("user1"));
		hoaxService.saveHoax(user, TestUtil.createValidHoax());
		hoaxService.saveHoax(user, TestUtil.createValidHoax());
		hoaxService.saveHoax(user, TestUtil.createValidHoax());
		ResponseEntity<TestPage<Object>> response = getHoaxes(new ParameterizedTypeReference<TestPage<Object>>() {}) ;
		
		assertThat(response.getBody().getTotalElements()).isEqualTo(3);
	}

	@Test
	public void getHoaxes_whenThereAreHoaxes_returnPageWithHoaxVm() {
		User user = userService.save(TestUtil.createValidUser("user1"));
		hoaxService.saveHoax(user, TestUtil.createValidHoax());
		ResponseEntity<TestPage<HoaxVm>> response = getHoaxes(new ParameterizedTypeReference<TestPage<HoaxVm>>() {}) ;
		HoaxVm storedHoax = response.getBody().getContent().get(0);
		assertThat(storedHoax.getUser().getUsername()).isEqualTo("user1");
	}
	
	@Test
	public void postHoax_whenUserIsAuthorized_receiveHoaxVm() {
		userService.save(TestUtil.createValidUser("user1"));
		authenticate("user1");
		Hoax hoax = TestUtil.createValidHoax();
		ResponseEntity<HoaxVm> response = postHoax( hoax, HoaxVm.class);
		assertThat(response.getBody().getUser().getUsername()).isEqualTo("user1");
	}
	
	@Test
	public void getHoaxesOfUser_whenUserExists_receiveOk() {
		userService.save(TestUtil.createValidUser("user1"));
		ResponseEntity<Object> response = getHoaxesOfUser("user1", new ParameterizedTypeReference<Object> () {});
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}
	
	@Test
	public void getHoaxesOfUser_whenUserDoesNotExists_receiveNotFound() {
		ResponseEntity<Object> response = getHoaxesOfUser("unknown-user", new ParameterizedTypeReference<Object> () {});
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}
	
	@Test
	public void getHoaxesOfUser_whenUserExists_receivePageWithZeroHoaxes() {
		userService.save(TestUtil.createValidUser("user1"));
		ResponseEntity<TestPage<Object>> response = getHoaxesOfUser("user1", new ParameterizedTypeReference<TestPage<Object>> () {});
		assertThat(response.getBody().getTotalElements()).isEqualTo(0);
	}
	
	@Test
	public void getHoaxesOfUser_whenTUserExistsWithHoaxes_returnPageWithHoaxVm() {
		User user = userService.save(TestUtil.createValidUser("user1"));
		hoaxService.saveHoax(user, TestUtil.createValidHoax());
		ResponseEntity<TestPage<HoaxVm>> response = getHoaxesOfUser("user1",new ParameterizedTypeReference<TestPage<HoaxVm>>() {}) ;
		HoaxVm storedHoax = response.getBody().getContent().get(0);
		assertThat(storedHoax.getUser().getUsername()).isEqualTo("user1");
	}
	
	@Test
	public void getHoaxesOfUser_whenUserExistsWithMultipleHoaxes_returnPageWithMatchingHoaxesCount() {
		User user = userService.save(TestUtil.createValidUser("user1"));
		hoaxService.saveHoax(user, TestUtil.createValidHoax());
		hoaxService.saveHoax(user, TestUtil.createValidHoax());
		hoaxService.saveHoax(user, TestUtil.createValidHoax());
		ResponseEntity<TestPage<HoaxVm>> response = getHoaxesOfUser("user1",new ParameterizedTypeReference<TestPage<HoaxVm>>() {}) ;
		
		assertThat(response.getBody().getTotalElements()).isEqualTo(3);
	}
	
	@Test
	public void getHoaxesOfUser_whenMultipleUserExistsWithMultipleHoaxes_returnPageWithMatchingHoaxesCount() {
		User userWithThreeHoaxes = userService.save(TestUtil.createValidUser("user1"));
		IntStream.rangeClosed(1, 3).forEach(i ->{
			hoaxService.saveHoax(userWithThreeHoaxes, TestUtil.createValidHoax());
		});
		
		User userWith5Hoaxes = userService.save(TestUtil.createValidUser("user2"));
		IntStream.rangeClosed(1, 5).forEach(i ->{
			hoaxService.saveHoax(userWith5Hoaxes, TestUtil.createValidHoax());
		});
		ResponseEntity<TestPage<HoaxVm>> response = getHoaxesOfUser(userWith5Hoaxes.getUsername(),new ParameterizedTypeReference<TestPage<HoaxVm>>() {}) ;
		
		assertThat(response.getBody().getTotalElements()).isEqualTo(5);
	}
	
	@Test
	public void getOldHoaxes_whenThereAreNoHoaxes_receiveOk() {
		ResponseEntity<Object> response = getOldHoaxes(5, new ParameterizedTypeReference<Object>() {});
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}
	
	@Test
	public void getOldHoaxes_whenThereAreHoaxes_receivePageWithItemsProvidedId() {
		User user = userService.save(TestUtil.createValidUser("user1"));
		hoaxService.saveHoax(user ,TestUtil.createValidHoax());
		hoaxService.saveHoax(user ,TestUtil.createValidHoax());
		hoaxService.saveHoax(user ,TestUtil.createValidHoax());
		Hoax fourth = hoaxService.saveHoax(user , TestUtil.createValidHoax());
		hoaxService.saveHoax(user ,TestUtil.createValidHoax());
		
		ResponseEntity<TestPage<Hoax>> response = getOldHoaxes(fourth.getId(), new ParameterizedTypeReference<TestPage<Hoax>>() {});
		assertThat(response.getBody().getTotalElements()).isEqualTo(3);
	}
	
	@Test
	public void getOldHoaxes_whenThereAreHoaxes_receivePageWithHoaxVmBeforeProvidedId() {
		User user = userService.save(TestUtil.createValidUser());
		hoaxService.saveHoax(user ,TestUtil.createValidHoax());
		hoaxService.saveHoax(user ,TestUtil.createValidHoax());
		hoaxService.saveHoax(user ,TestUtil.createValidHoax());
		Hoax fourth = hoaxService.saveHoax(user , TestUtil.createValidHoax());
		hoaxService.saveHoax(user ,TestUtil.createValidHoax());
		
		ResponseEntity<TestPage<HoaxVm>> response = getOldHoaxes(fourth.getId(), new ParameterizedTypeReference<TestPage<HoaxVm>>() {});
		assertThat(response.getBody().getContent().get(0).getDate()).isGreaterThan(0);
	}
	
	@Test
	public void getOldHoaxesOfUser_whenUserExistsAndThereAreNoHoaxes_receiveOk() {
		userService.save(TestUtil.createValidUser("user1"));
		ResponseEntity<Object> response = getOldHoaxesOfUser(5, "user1" ,new ParameterizedTypeReference<Object>() {});
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}
	
	@Test
	public void getOldHoaxesOfUser_whenUserExistAndThereAreHoaxes_receivePageWithItemsBeforeProvidedId() {
		User user = userService.save(TestUtil.createValidUser("user1"));
		hoaxService.saveHoax(user, TestUtil.createValidHoax());
		hoaxService.saveHoax(user, TestUtil.createValidHoax());
		hoaxService.saveHoax(user, TestUtil.createValidHoax());
		Hoax fourth = hoaxService.saveHoax(user, TestUtil.createValidHoax());
		hoaxService.saveHoax(user, TestUtil.createValidHoax());
		
		ResponseEntity<TestPage<Object>> response = getOldHoaxesOfUser(fourth.getId(), "user1", new ParameterizedTypeReference<TestPage<Object>>() {});
		assertThat(response.getBody().getTotalElements()).isEqualTo(3);
	}
	
	@Test
	public void getOldHoaxesOfUser_whenUserExistAndThereAreHoaxes_receivePageWithHoaxVMBeforeProvidedId() {
		User user = userService.save(TestUtil.createValidUser("user1"));
		hoaxService.saveHoax(user, TestUtil.createValidHoax());
		hoaxService.saveHoax(user, TestUtil.createValidHoax());
		hoaxService.saveHoax(user, TestUtil.createValidHoax());
		Hoax fourth = hoaxService.saveHoax(user, TestUtil.createValidHoax());
		hoaxService.saveHoax(user, TestUtil.createValidHoax());
		
		ResponseEntity<TestPage<HoaxVm>> response = getOldHoaxesOfUser(fourth.getId(), "user1", new ParameterizedTypeReference<TestPage<HoaxVm>>() {});
		assertThat(response.getBody().getContent().get(0).getDate()).isGreaterThan(0);
	}
	
	@Test
	public void getOldHoaxesOfUser_whenUserDoesNotExistsAndThereAreNoHoaxes_receiveNotFoud() {
		
		ResponseEntity<Object> response = getOldHoaxesOfUser(5, "user1" ,new ParameterizedTypeReference<Object>() {});
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}
	
	@Test
	public void getOldHoaxesOfUser_whenUserExistAndThereAreNoHoaxes_receivePageWithZeroItemsBeforeProvidedId() {
		User user = userService.save(TestUtil.createValidUser("user1"));
		hoaxService.saveHoax(user, TestUtil.createValidHoax());
		hoaxService.saveHoax(user, TestUtil.createValidHoax());
		hoaxService.saveHoax(user, TestUtil.createValidHoax());
		Hoax fourth = hoaxService.saveHoax(user, TestUtil.createValidHoax());
		hoaxService.saveHoax(user, TestUtil.createValidHoax());
		
		userService.save(TestUtil.createValidUser("user2"));
		
		ResponseEntity<TestPage<HoaxVm>> response = getOldHoaxesOfUser(fourth.getId(), "user2", new ParameterizedTypeReference<TestPage<HoaxVm>>() {});
		assertThat(response.getBody().getTotalElements()).isEqualTo(0);
	}
	
	@Test
	public void getNewHoaxes_whenThereAreHoaxes_receiveListWithItemsAfterProvidedId() {
		User user = userService.save(TestUtil.createValidUser("user1"));
		hoaxService.saveHoax(user ,TestUtil.createValidHoax());
		hoaxService.saveHoax(user ,TestUtil.createValidHoax());
		hoaxService.saveHoax(user ,TestUtil.createValidHoax());
		Hoax fourth = hoaxService.saveHoax(user , TestUtil.createValidHoax());
		hoaxService.saveHoax(user ,TestUtil.createValidHoax());
		
		ResponseEntity<List<Object>> response = getNewHoaxes(fourth.getId(), new ParameterizedTypeReference<List<Object>>() {});
		assertThat(response.getBody().size()).isEqualTo(1);
	}
	
	@Test
	public void getNewHoaxes_whenThereAreHoaxes_receiveListOfHoaxVmItemsAfterProvidedId() {
		User user = userService.save(TestUtil.createValidUser("user1"));
		hoaxService.saveHoax(user ,TestUtil.createValidHoax());
		hoaxService.saveHoax(user ,TestUtil.createValidHoax());
		hoaxService.saveHoax(user ,TestUtil.createValidHoax());
		Hoax fourth = hoaxService.saveHoax(user , TestUtil.createValidHoax());
		hoaxService.saveHoax(user ,TestUtil.createValidHoax());
		
		ResponseEntity<List<HoaxVm>> response = getNewHoaxes(fourth.getId(), new ParameterizedTypeReference<List<HoaxVm>>() {});
		assertThat(response.getBody().get(0).getDate()).isGreaterThan(0);
	}
	// novi
	@Test
	public void getNewHoaxesOfUser_whenUserExistsAndThereAreNoHoaxes_receiveOk() {
		userService.save(TestUtil.createValidUser("user1"));
		ResponseEntity<Object> response = getNewHoaxesOfUser(5, "user1" ,new ParameterizedTypeReference<Object>() {});
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}
	
	@Test
	public void getNewHoaxesOfUser_whenUserExistAndThereAreHoaxes_receiveListWithItemsAfterProvidedId() {
		User user = userService.save(TestUtil.createValidUser("user1"));
		hoaxService.saveHoax(user, TestUtil.createValidHoax());
		hoaxService.saveHoax(user, TestUtil.createValidHoax());
		hoaxService.saveHoax(user, TestUtil.createValidHoax());
		Hoax fourth = hoaxService.saveHoax(user, TestUtil.createValidHoax());
		hoaxService.saveHoax(user, TestUtil.createValidHoax());
		
		ResponseEntity<List<Object>> response = getNewHoaxesOfUser(fourth.getId(), "user1", new ParameterizedTypeReference<List<Object>>() {});
		assertThat(response.getBody().size()).isEqualTo(1);
	}
	
	@Test
	public void getNewHoaxesOfUser_whenUserExistAndThereAreHoaxes_receiveListWithHoaxVMAfterProvidedId() {
		User user = userService.save(TestUtil.createValidUser("user1"));
		hoaxService.saveHoax(user, TestUtil.createValidHoax());
		hoaxService.saveHoax(user, TestUtil.createValidHoax());
		hoaxService.saveHoax(user, TestUtil.createValidHoax());
		Hoax fourth = hoaxService.saveHoax(user, TestUtil.createValidHoax());
		hoaxService.saveHoax(user, TestUtil.createValidHoax());
		
		ResponseEntity<List<HoaxVm>> response = getNewHoaxesOfUser(fourth.getId(), "user1", new ParameterizedTypeReference<List<HoaxVm>>() {});
		assertThat(response.getBody().get(0).getDate()).isGreaterThan(0);
	}
	
	
	
	@Test
	public void getNewHoaxesOfUser_whenUserDoesNotExistsAndThereAreNoHoaxes_receiveNotFoud() {
		
		ResponseEntity<Object> response = getNewHoaxesOfUser(5, "user1" ,new ParameterizedTypeReference<Object>() {});
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}
	
	@Test
	public void getNewHoaxesOfUser_whenUserExistAndThereAreNoHoaxes_receiveListWithZeroItemsAfterProvidedId() {
		User user = userService.save(TestUtil.createValidUser("user1"));
		hoaxService.saveHoax(user, TestUtil.createValidHoax());
		hoaxService.saveHoax(user, TestUtil.createValidHoax());
		hoaxService.saveHoax(user, TestUtil.createValidHoax());
		Hoax fourth = hoaxService.saveHoax(user, TestUtil.createValidHoax());
		hoaxService.saveHoax(user, TestUtil.createValidHoax());
		
		userService.save(TestUtil.createValidUser("user2"));
		
		ResponseEntity<List<HoaxVm>> response = getNewHoaxesOfUser(fourth.getId(), "user2", new ParameterizedTypeReference<List<HoaxVm>>() {});
		assertThat(response.getBody().size()).isEqualTo(0);
	}
	
	@Test
	public void getNewHoaxCount_whenThereAreHoaxes_receiveCountAfterProvidedId() {
		User user = userService.save(TestUtil.createValidUser("user1"));
		hoaxService.saveHoax(user ,TestUtil.createValidHoax());
		hoaxService.saveHoax(user ,TestUtil.createValidHoax());
		hoaxService.saveHoax(user ,TestUtil.createValidHoax());
		Hoax fourth = hoaxService.saveHoax(user , TestUtil.createValidHoax());
		hoaxService.saveHoax(user ,TestUtil.createValidHoax());
		
		ResponseEntity<Map<String , Integer>> response = getNewHoaxCount(fourth.getId(), new ParameterizedTypeReference<Map<String , Integer>>() {});
		assertThat(response.getBody().get("count")).isEqualTo(1);
	}
	
	@Test
	public void getNewHoaxCountOfUser_whenThereAreHoaxes_receiveCountAfterProvidedId() {
		User user = userService.save(TestUtil.createValidUser("user1"));
		hoaxService.saveHoax(user ,TestUtil.createValidHoax());
		hoaxService.saveHoax(user ,TestUtil.createValidHoax());
		hoaxService.saveHoax(user ,TestUtil.createValidHoax());
		Hoax fourth = hoaxService.saveHoax(user , TestUtil.createValidHoax());
		hoaxService.saveHoax(user ,TestUtil.createValidHoax());
		
		ResponseEntity<Map<String , Integer>> response = getNewHoaxCountOfUser(fourth.getId(),"user1", new ParameterizedTypeReference<Map<String , Integer>>() {});
		assertThat(response.getBody().get("count")).isEqualTo(1);
	}
	
	@Test
	public void postHoax_whenHoaxHasFileAttachmentAndUserIsAuthorized_fileAttachmentHoaxRelationIsUpdatedInDataBase() throws IOException {
		userService.save(TestUtil.createValidUser("user1"));
		authenticate("user1");
		
		MultipartFile file = createFile();
		
		FileAttachment savedFile = fileService.saveAttachment(file);
		
		Hoax hoax = TestUtil.createValidHoax();
		hoax.setAttachment(savedFile);
		ResponseEntity<HoaxVm> response = postHoax(hoax, HoaxVm.class);
		
		FileAttachment inDB = fileAttachmentRepository.findAll().get(0);
		assertThat(inDB.getHoax().getId()).isEqualTo(response.getBody().getId());
	}
	
	@Test
	public void postHoax_whenHoaxHasFileAttachmentAndUserIsAuthorized_fileAttachmentRelationIsUpdatedInDataBase() throws IOException {
		userService.save(TestUtil.createValidUser("user1"));
		authenticate("user1");
		
		MultipartFile file = createFile();
		
		FileAttachment savedFile = fileService.saveAttachment(file);
		
		Hoax hoax = TestUtil.createValidHoax();
		hoax.setAttachment(savedFile);
		ResponseEntity<HoaxVm> response = postHoax(hoax, HoaxVm.class);
		
		Hoax inDb = hoaxRepository.findById(response.getBody().getId()).get();
		assertThat(inDb.getAttachment().getId()).isEqualTo(savedFile.getId());
	}
	
	@Test
	public void postHoax_whenHoaxHasFileAttachmentAndUserIsAuthorized_receiveHoaxVmWithAttachment() throws IOException {
		userService.save(TestUtil.createValidUser("user1"));
		authenticate("user1");
		
		MultipartFile file = createFile();
		
		FileAttachment savedFile = fileService.saveAttachment(file);
		
		Hoax hoax = TestUtil.createValidHoax();
		hoax.setAttachment(savedFile);
		ResponseEntity<HoaxVm> response = postHoax(hoax, HoaxVm.class);
		
		
		assertThat(response.getBody().getAttachment().getName()).isEqualTo(savedFile.getName());
	}
	
	@Test
	public void deleteHoax_whenUserIsUnauthorized_receiveUnauothorized() {
		ResponseEntity<Object> response = deleteHoax(555, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}
	
	@Test
	public void deleteHoax_whenUserIsAuthorized_receiveOk() {
		User user = userService.save(TestUtil.createValidUser("user1"));
		authenticate("user1");
		Hoax hoax = hoaxService.saveHoax(user ,TestUtil.createValidHoax());
		ResponseEntity<Object> response = deleteHoax(hoax.getId(), Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}
	
	@Test
	public void deleteHoax_whenUserIsAuthorized_receiveGenericResponse() {
		User user = userService.save(TestUtil.createValidUser("user1"));
		authenticate("user1");
		Hoax hoax = hoaxService.saveHoax(user ,TestUtil.createValidHoax());
		ResponseEntity<GenericResponse> response = deleteHoax(hoax.getId(), GenericResponse.class);
		assertThat(response.getBody().getMessage()).isNotNull();
	}
	
	@Test
	public void deleteHoax_whenHoaxIsOwnedByAnotherUser_receiveForbiden() {
		userService.save(TestUtil.createValidUser("user1"));
		authenticate("user1");
		User user2 = userService.save(TestUtil.createValidUser("user2"));
		authenticate("user2");
		Hoax hoax = hoaxService.saveHoax(user2 ,TestUtil.createValidHoax());
		ResponseEntity<Object> response = deleteHoax(hoax.getId(), Object.class);
		
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}
	
	@Test
	public void deleteHoax_whenUserIsAuthorized_hoaxRemovedFromDatabase() {
		User user = userService.save(TestUtil.createValidUser("user1"));
		authenticate("user1");
		Hoax hoax = hoaxService.saveHoax(user ,TestUtil.createValidHoax());
		deleteHoax(hoax.getId(), Object.class);
		Optional<Hoax> inDb = hoaxRepository.findById(hoax.getId());
		assertThat(inDb.isPresent()).isFalse();
	}
	
	@Test
	public void deleteHoax_whenHoaxNotExist_receiveForbiden() {
		userService.save(TestUtil.createValidUser("user1"));
		authenticate("user1");
		
		ResponseEntity<Object> response = deleteHoax(5555, Object.class);
		
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}
	
	@Test
	public void deleteHoax_whenHoaxHasAttachment_attachmentIsRemovedFromDatabase() throws IOException {
		userService.save(TestUtil.createValidUser("user1"));
		authenticate("user1");
		
		MultipartFile file = createFile();
		
		FileAttachment savedFile = fileService.saveAttachment(file);
		
		Hoax hoax = TestUtil.createValidHoax();
		hoax.setAttachment(savedFile);
		ResponseEntity<HoaxVm> response = postHoax(hoax, HoaxVm.class);
		
		Integer hoaxId = response.getBody().getId();
		
		deleteHoax(hoaxId, Object.class);
		
		Optional<FileAttachment> attachment = fileAttachmentRepository.findById(savedFile.getId());
		
		assertThat(attachment.isPresent()).isFalse();
	}
	
	@Test
	public void deleteHoax_whenHoaxHasAttachment_attachmentIsRemovedFromStorage() throws IOException {
		userService.save(TestUtil.createValidUser("user1"));
		authenticate("user1");
		
		MultipartFile file = createFile();
		
		FileAttachment savedFile = fileService.saveAttachment(file);
		
		Hoax hoax = TestUtil.createValidHoax();
		hoax.setAttachment(savedFile);
		ResponseEntity<HoaxVm> response = postHoax(hoax, HoaxVm.class);
		
		Integer hoaxId = response.getBody().getId();
		
		deleteHoax(hoaxId, Object.class);
		String attachmentPath = appConfiguration.getFullAttachmentsPath() + "/" + savedFile.getName();
		
		File storedImage = new File(attachmentPath);
		
		assertThat(storedImage.exists()).isFalse();
	}


	private MultipartFile createFile() throws IOException {
		ClassPathResource imageResource = new ClassPathResource("profile.png");
		byte[] fileAsByte = FileUtils.readFileToByteArray(imageResource.getFile());
		
		MultipartFile file = new MockMultipartFile("profile.png", fileAsByte);
		return file;
	}
	
	public <T> ResponseEntity<T> deleteHoax(Integer hoaxId, Class<T> responseType){
		return testRestTemplate.exchange(API_1_0_HOAXES + "/" + hoaxId, HttpMethod.DELETE, null, responseType);
	}
	
	public <T> ResponseEntity<T> getNewHoaxCount(Integer hoaxId, ParameterizedTypeReference<T> responseType){
		String path = API_1_0_HOAXES + "/" + hoaxId +"?direction=after&count=true";
		return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
	}

	public <T> ResponseEntity<T> getNewHoaxCountOfUser(Integer hoaxId, String username, ParameterizedTypeReference<T> responseType){
		String path = "/api/1.0/users/" + username + "/hoaxes/" + hoaxId +"?direction=after&count=true";
		return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
	}

	
	public <T> ResponseEntity<T> getNewHoaxes(Integer hoaxId, ParameterizedTypeReference<T> responseType){
		String path = API_1_0_HOAXES + "/" + hoaxId +"?direction=after&sort=id,desc";
		return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
	}

	public <T> ResponseEntity<T> getNewHoaxesOfUser(Integer hoaxId, String username, ParameterizedTypeReference<T> responseType){
		String path = "/api/1.0/users/" + username + "/hoaxes/" + hoaxId +"?direction=after&sort=id,desc";
		return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
	}
	
	public <T> ResponseEntity<T> getOldHoaxes(Integer hoaxId, ParameterizedTypeReference<T> responseType){
		String path = API_1_0_HOAXES + "/" + hoaxId +"?direction=before&page=0&size=5&sort=id,desc";
		return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
	}

	public <T> ResponseEntity<T> getOldHoaxesOfUser(Integer hoaxId, String username, ParameterizedTypeReference<T> responseType){
		String path = "/api/1.0/users/" + username + "/hoaxes/" + hoaxId +"?direction=before&page=0&size=5&sort=id,desc";
		return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
	}
	
	public <T> ResponseEntity<T> getHoaxesOfUser(String username, ParameterizedTypeReference<T> responseType){
		String path = "/api/1.0/users/" + username + "/hoaxes";
		return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
	}

	public <T> ResponseEntity<T> getHoaxes(ParameterizedTypeReference<T> responseType){
		return testRestTemplate.exchange(API_1_0_HOAXES, HttpMethod.GET, null, responseType);
	}
	
	private <T> ResponseEntity<T> postHoax(Hoax hoax, Class<T> responseType) {
		return testRestTemplate.postForEntity(API_1_0_HOAXES, hoax, responseType);
	}
	
	@AfterEach
	public void cleanupAfter() {
		fileAttachmentRepository.deleteAll();
		hoaxRepository.deleteAll();
	}
}


