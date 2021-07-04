package com.example.bashir.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;

import com.example.bashir.configuration.AppConfiguration;

@Service
public class FileService {
	
	AppConfiguration appConfiguration;
	
	Tika tika;

	public FileService(AppConfiguration appConfiguration) {
		super();
		this.appConfiguration = appConfiguration;
		this.tika = new Tika();
	}
	
	public String saveProfileImage(String base64) throws IOException {
		String imageName = UUID.randomUUID().toString().replaceAll("-", "");
		
		byte[] decodedByBites = Base64.getDecoder().decode(base64);
		File target = new File(appConfiguration.getFullProfileImagesPath()+ "/"+ imageName);
		FileUtils.writeByteArrayToFile(target, decodedByBites);
		return imageName;
		
	}

	public String detectType(byte[] fileArr) {
		
		return tika.detect(fileArr);
	}

	public void deleteProfileImage(String image) {
		
		try {
			Files.deleteIfExists(Paths.get(appConfiguration.getFullProfileImagesPath() + "/" + image));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
