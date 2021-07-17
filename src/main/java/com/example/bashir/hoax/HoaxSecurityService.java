package com.example.bashir.hoax;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.bashir.user.User;

@Service
public class HoaxSecurityService {
	
	HoaxRepository hoaxRepository;
	
	
	public HoaxSecurityService(HoaxRepository hoaxRepository) {
		super();
		this.hoaxRepository = hoaxRepository;
	}


	public boolean isAllowedToDelete(Integer hoaxId, User loggedInUser) {
		Optional<Hoax> optionalHoax = hoaxRepository.findById(hoaxId);
		if(optionalHoax.isPresent()) {
			Hoax inDb = optionalHoax.get();
			return inDb.getUser().getId() == loggedInUser.getId() ;
				
			}
		return false;
		}
	}


