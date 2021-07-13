package com.example.bashir.file;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.example.bashir.hoax.Hoax;

import lombok.Data;

@Entity
@Data
public class FileAttachment {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date date;
	
	private String name;
	
	private String fileType;
	
	@OneToOne
	private Hoax hoax;

}
