package com.example.bashir.hoax;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.bashir.shared.CurrentUser;
import com.example.bashir.shared.GenericResponse;
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
	
	@GetMapping({"/hoaxes/{id:[0-9]+}", "/users/{username}/hoaxes/{id:[0-9]+}"} )
	ResponseEntity<?> getHoaxesRelative(@PathVariable Integer id,
			@PathVariable(required = false) String username,
			Pageable pageable,
			@RequestParam(name = "direction", defaultValue = "after")String direction,
			@RequestParam(name = "count", defaultValue = "false", required = false)Boolean count) {
		if(!direction.equalsIgnoreCase("after")) {
		return ResponseEntity.ok(hoaxService.getOldHoaxes(id, username, pageable).map(HoaxVm::new));
		}
		
		if(count == true) {
			Integer newHoaxCount = hoaxService.getNewHoaxesCount(id, username);
			return ResponseEntity.ok(Collections.singletonMap("count", newHoaxCount));
		}
		 List<HoaxVm> newHoaxes =hoaxService.getNewHoaxes(id,username, pageable).stream().map(HoaxVm::new).collect(Collectors.toList());
		 return ResponseEntity.ok(newHoaxes);
	}
	/*
	@GetMapping("/users/{username}/hoaxes/{id:[0-9]+}")
	ResponseEntity<?> getHoaxesRelativeOfUser(@PathVariable String username,@PathVariable Integer id, Pageable pageable,
			@RequestParam(name = "direction", defaultValue = "after")String direction,
			@RequestParam(name = "count", defaultValue = "false", required = false)Boolean count) {
		if(!direction.equalsIgnoreCase("after")) {
		return ResponseEntity.ok(hoaxService.getOldHoaxesOfUser(id,username, pageable).map(HoaxVm::new));
		}
		
		if(count == true) {
			Integer newHoaxCountOfUser = hoaxService.getNewHoaxCountOfUser(id, username);
			return ResponseEntity.ok(Collections.singletonMap("count", newHoaxCountOfUser));
		}
		List<HoaxVm> newHoaxes = hoaxService.getNewHoaxesOfUser(id, username,pageable).stream().map(HoaxVm::new).collect(Collectors.toList());
		return ResponseEntity.ok(newHoaxes);
	}
	*/
	@DeleteMapping({"/hoaxes/{id:[0-9]+}"})
	@PreAuthorize("@hoaxSecurityService.isAllowedToDelete(#id, principal)")
	GenericResponse deleteHoax(@PathVariable Integer id) {
		hoaxService.deleteHoax(id);
		return new GenericResponse("Hoax is removed");
		
	}
	
}
