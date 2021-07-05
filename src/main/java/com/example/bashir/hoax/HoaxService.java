package com.example.bashir.hoax;

import java.util.Date;

import org.springframework.stereotype.Service;


@Service
public class HoaxService {

	HoaxRepository hoaxRepository;

	public HoaxService(HoaxRepository hoaxRepository) {
		super();
		this.hoaxRepository = hoaxRepository;
	}
	
	public void saveHoax(Hoax hoax) {
		hoax.setTimestamp(new Date());
		hoaxRepository.save(hoax);
	}
}
