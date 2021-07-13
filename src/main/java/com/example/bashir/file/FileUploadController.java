package com.example.bashir.file;

import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.bashir.user.UserService;

@RestController
@RequestMapping("/api/1.0")
public class FileUploadController {
	
	@Autowired
	UserService userService;
	
	@Autowired
	FileService fileService;
	
	@PostMapping("/hoaxes/upload")
	FileAttachment upload(MultipartFile file) {
		return fileService.saveAttachment(file);
		
	}

}
