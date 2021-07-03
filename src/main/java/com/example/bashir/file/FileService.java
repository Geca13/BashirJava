package com.example.bashir.file;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import com.example.bashir.configuration.AppConfiguration;

@Service
public class FileService {
	
	AppConfiguration appConfiguration;

	public FileService(AppConfiguration appConfiguration) {
		super();
		this.appConfiguration = appConfiguration;
	}
	
	public String saveProfileImage(String base64) throws IOException {
		String imageName = UUID.randomUUID().toString().replaceAll("-", "");
		
		byte[] decodedByBites = Base64.getDecoder().decode(base64);
		File target = new File(appConfiguration.getProfileImagesFolder()+ "/"+ imageName);
		FileUtils.writeByteArrayToFile(target, decodedByBites);
		return imageName;
		
	}

}
