package com.example.bashir.hoax;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bashir.shared.CurrentUser;
import com.example.bashir.user.User;

@RestController
@RequestMapping("/api/1.0")
public class HoaxController {
	
	@Autowired
	HoaxService hoaxService;
	
	@PostMapping("/hoaxes")
	HoaxVm createHoax(@Valid @RequestBody Hoax hoax, @CurrentUser User user) {
		return new HoaxVm( hoaxService.saveHoax(user, hoax));
	}
	
	@GetMapping("/hoaxes")
	Page<HoaxVm> getAllHoaxes(Pageable pageable) {
		return hoaxService.getAllHoaxes(pageable).map(HoaxVm::new);
	}
	
	@GetMapping("/users/{username}/hoaxes")
	Page<HoaxVm> getHoaxesOfUser(@PathVariable String username, Pageable pageable) {
		return hoaxService.getHoaxesOfUser(username, pageable).map(HoaxVm::new);
		
	}
	
	@GetMapping("/hoaxes/{id:[0-9]+}")
	Page<?> getHoaxesRelative(@PathVariable Integer id, Pageable pageable) {
		return hoaxService.getOldHoaxes(id, pageable).map(HoaxVm::new);
		
	}
	
	@GetMapping("/users/{username}/hoaxes/{id:[0-9]+}")
	Page<?> getHoaxesRelativeOfUser(@PathVariable String username,@PathVariable Integer id, Pageable pageable) {
		return hoaxService.getOldHoaxesOfUser(id,username, pageable).map(HoaxVm::new);
		
	}
	

}
