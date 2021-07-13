package com.example.bashir.file;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileAttachmentRepository extends JpaRepository<FileAttachment, Integer> {
	
	List<FileAttachment> findByDateBeforeAndHoaxIsNull(Date date);

}
