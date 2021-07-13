package com.example.bashir.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.tika.Tika;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.bashir.configuration.AppConfiguration;

@Service
@EnableScheduling
public class FileService {
	
	AppConfiguration appConfiguration;
	
	FileAttachmentRepository fileAttachmentRepository;
	
	Tika tika;

	public FileService(AppConfiguration appConfiguration, FileAttachmentRepository fileAttachmentRepository) {
		super();
		this.appConfiguration = appConfiguration;
		this.fileAttachmentRepository = fileAttachmentRepository;
		this.tika = new Tika();
	}
	
	public String saveProfileImage(String base64) throws IOException {
		String imageName = getRandomName();
		
		byte[] decodedByBites = Base64.getDecoder().decode(base64);
		File target = new File(appConfiguration.getFullProfileImagesPath()+ "/"+ imageName);
		FileUtils.writeByteArrayToFile(target, decodedByBites);
		return imageName;
		
	}

	private String getRandomName() {
		String imageName = UUID.randomUUID().toString().replaceAll("-", "");
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

	public FileAttachment saveAttachment(MultipartFile file) {
		FileAttachment attachment = new FileAttachment();
		attachment.setDate(new Date());
		String randomName = getRandomName();
		attachment.setName(randomName);
		File target = new File(appConfiguration.getFullAttachmentsPath() + "/" + randomName);
		try {
			byte [] fileAsByte = file.getBytes();
			FileUtils.writeByteArrayToFile(target, fileAsByte);
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		return attachment;
    }

	@Scheduled(fixedRate = 60 * 60 * 1000)
	public void cleanupStorage() {
		Date oneHourAgo = new Date(System.currentTimeMillis() - (60*60*1000));
		List<FileAttachment> oldFiles = fileAttachmentRepository.findByDateBeforeAndHoaxIsNull(oneHourAgo);
		for(FileAttachment file: oldFiles) {
			deleteAttachmentImage(file.getName());
			fileAttachmentRepository.deleteById(file.getId());
		}
		
	}

	public void deleteAttachmentImage(String image) {
		try {
			Files.deleteIfExists(Paths.get(appConfiguration.getFullAttachmentsPath()+"/"+image));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
