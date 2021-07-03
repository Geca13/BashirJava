package com.example.bashir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.bashir.configuration.AppConfiguration;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class StaticResourceTest {
	
	@Autowired
	AppConfiguration appConfiguration;
	
	@Autowired
	MockMvc mockMvc;
	
	@Test
	public void checkStaticFolder_whenAppIsInitialized_uploadFolderMustExist() {
		File uploadFolder = new File(appConfiguration.getUploadPath());
		boolean uploadFolderExist = uploadFolder.exists() && uploadFolder.isDirectory();
		assertThat(uploadFolderExist).isTrue();
	}
	
	@Test
	public void checkStaticFolder_whenAppIsInitialized_profileImageSubfolderMustExist() {
		String profileImageFolderPath = appConfiguration.getFullProfileImagesPath();
		File profileImageFolder = new File(profileImageFolderPath);
		boolean profileImageFolderExists = profileImageFolder.exists() && profileImageFolder.isDirectory();
		assertThat(profileImageFolderExists).isTrue();
	}
	
	@Test
	public void checkStaticFolder_whenAppIsInitialized_attachmentsSubfolderMustExist() {
		String attachmentsFolderPath = appConfiguration.getFullAttachmentsPath();
		File attachmentsImageFolder = new File(attachmentsFolderPath);
		boolean attachmentsImageFolderExists = attachmentsImageFolder.exists() && attachmentsImageFolder.isDirectory();
		assertThat(attachmentsImageFolderExists).isTrue();
	}
	
	@Test
	public void getStaticFile_whenImageExistsInProfileUploadFolder_receiveOk() throws Exception {
		String fileName = "profile-picture.png";
		File source = new ClassPathResource("profile.png").getFile();
		
		File target = new File(appConfiguration.getFullProfileImagesPath() + "/" + fileName);
		FileUtils.copyFile(source, target);
		
		mockMvc.perform(get("/images/" +appConfiguration.getProfileImagesFolder() + "/" + fileName)).andExpect(status().isOk());
		
	}
	
	@Test
	public void getStaticFile_whenAttachmentExistsInProfileUploadFolder_receiveOk() throws Exception {
		String fileName = "profile-picture.png";
		File source = new ClassPathResource("profile.png").getFile();
		
		File target = new File(appConfiguration.getFullAttachmentsPath() + "/" + fileName);
		FileUtils.copyFile(source, target);
		
		mockMvc.perform(get("/images/" +appConfiguration.getAttachmentsFolder() + "/" + fileName)).andExpect(status().isOk());
		
	}
	
	@Test
	public void getStaticFile_whenImageDoesNotExist_receiveNotFound() throws Exception {
		mockMvc.perform(get("/images/" + appConfiguration.getAttachmentsFolder() + "/there-is-no-such-image.png"))
		.andExpect(status().isNotFound());
		
	}
	
	@Test
	public void getStaticFile_whenAttachmentExistsInProfileUploadFolder_receiveOkWithCacheHeaders() throws Exception {
		String fileName = "profile-picture.png";
		File source = new ClassPathResource("profile.png").getFile();
		
		File target = new File(appConfiguration.getFullAttachmentsPath() + "/" + fileName);
		FileUtils.copyFile(source, target);
		
		MvcResult result = mockMvc.perform(get("/images/" +appConfiguration.getAttachmentsFolder() + "/" + fileName)).andExpect(status().isOk()).andReturn();
		
		String cacheControl = result.getResponse().getHeaderValue("Cache-Control").toString();
		
		assertThat(cacheControl).containsIgnoringCase("max-age=31536000");
	}
	
	
	@AfterEach
	public void cleanUp() throws IOException {
		FileUtils.cleanDirectory(new File(appConfiguration.getFullProfileImagesPath()));
		FileUtils.cleanDirectory(new File(appConfiguration.getFullAttachmentsPath()));
	}
	
	

}
