package com.example.bashir.hoax;

import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.bashir.user.User;
import com.example.bashir.user.UserRepository;
import com.example.bashir.user.UserService;




@Service
public class HoaxService {

	HoaxRepository hoaxRepository;
	
	UserService userService;

	public HoaxService(HoaxRepository hoaxRepository, UserService userService) {
		super();
		this.hoaxRepository = hoaxRepository;
		this.userService = userService;
	}
	
	public Hoax saveHoax( User user, Hoax hoax) {
		hoax.setTimestamp(new Date());
		hoax.setUser(user);
		return hoaxRepository.save(hoax);
	}

	public Page<Hoax> getAllHoaxes(Pageable pageable) {
		
		return hoaxRepository.findAll(pageable);
	}

	public Page<Hoax> getHoaxesOfUser(String username, Pageable pageable) {
		User inDb = userService.getByUsername(username);
		return hoaxRepository.findByUser(inDb,pageable);
		
		
	}

	public Page<Hoax> getOldHoaxes(Integer id, Pageable pageable) {
		return hoaxRepository.findByIdLessThan(id, pageable);
	}

	public Page<Hoax> getOldHoaxesOfUser(Integer id, String username, Pageable pageable) {
		User inDb = userService.getByUsername(username);
		return hoaxRepository.findByIdLessThanAndUser(id, inDb, pageable);
	}
}
