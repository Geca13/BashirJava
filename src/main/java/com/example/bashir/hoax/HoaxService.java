package com.example.bashir.hoax;

import java.util.Date;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.bashir.file.FileAttachment;
import com.example.bashir.file.FileAttachmentRepository;
import com.example.bashir.user.User;
import com.example.bashir.user.UserRepository;
import com.example.bashir.user.UserService;




@Service
public class HoaxService {

	HoaxRepository hoaxRepository;
	
	UserService userService;
	
	FileAttachmentRepository fileAttachmentRepository;

	public HoaxService(HoaxRepository hoaxRepository, UserService userService,FileAttachmentRepository fileAttachmentRepository) {
		super();
		this.hoaxRepository = hoaxRepository;
		this.userService = userService;
		this.fileAttachmentRepository = fileAttachmentRepository;
	}
	
	public Hoax saveHoax( User user, Hoax hoax) {
		hoax.setTimestamp(new Date());
		hoax.setUser(user);
		if(hoax.getAttachment() != null) {
			FileAttachment inDB = fileAttachmentRepository.findById(hoax.getAttachment().getId()).get();
			inDB.setHoax(hoax);
			hoax.setAttachment(inDB);
		}
		return hoaxRepository.save(hoax);
	}

	public Page<Hoax> getAllHoaxes(Pageable pageable) {
		
		return hoaxRepository.findAll(pageable);
	}

	public Page<Hoax> getHoaxesOfUser(String username, Pageable pageable) {
		User inDb = userService.getByUsername(username);
		return hoaxRepository.findByUser(inDb,pageable);
		
		
	}

	public Page<Hoax> getOldHoaxes(Integer id, String username, Pageable pageable) {
		Specification<Hoax> spec = Specification.where(idLessThan(id));
		if(username != null) {
			User inDb = userService.getByUsername(username);
		    spec = spec.and(userIs(inDb));
		}
		return hoaxRepository.findAll(spec, pageable);
	}
/*
	public Page<Hoax> getOldHoaxesOfUser(Integer id, String username, Pageable pageable) {
		User inDb = userService.getByUsername(username);
		return hoaxRepository.findByIdLessThanAndUser(id, inDb, pageable);
	}
*/
	public List<Hoax> getNewHoaxes(Integer id, String username, Pageable pageable) {
		Specification<Hoax> spec = Specification.where(idGreaterThan(id));
		if(username != null) {
			User inDb = userService.getByUsername(username);
		    spec = spec.and(userIs(inDb));
		}
		return hoaxRepository.findAll(spec, pageable.getSort());
	}
/*
	public List<Hoax> getNewHoaxesOfUser(Integer id, String username, Pageable pageable) {
		User inDb = userService.getByUsername(username);
		return hoaxRepository.findByIdGreaterThanAndUser(id, inDb, pageable.getSort());
	}
*/
	public Integer getNewHoaxesCount(Integer id, String username) {
		if(username == null) {
		return hoaxRepository.countByIdGreaterThan(id);
		}
		User inDb = userService.getByUsername(username);
		return hoaxRepository.countByIdGreaterThanAndUser(id, inDb);
	}
/*
	public Integer getNewHoaxCountOfUser(Integer id, String username) {
		User inDb = userService.getByUsername(username);
		return hoaxRepository.countByIdGreaterThanAndUser(id, inDb);
	}
	*/
	
	private Specification<Hoax> userIs(User user){
		return (root, query, criteriaBuilder)->{
			return criteriaBuilder.equal(root.get("user") , user);
		};
	}
	
	private Specification<Hoax> idLessThan(Integer id){
		return (root, query, criteriaBuilder)->{
			return criteriaBuilder.lessThan(root.get("id"), id);
		};
	}
	
	private Specification<Hoax> idGreaterThan(Integer id){
		return (root, query, criteriaBuilder)->{
			return criteriaBuilder.greaterThan(root.get("id"), id);
		};
	}
}

    
