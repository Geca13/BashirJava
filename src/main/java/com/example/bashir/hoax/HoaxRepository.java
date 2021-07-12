package com.example.bashir.hoax;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.bashir.user.User;

@Repository
public interface HoaxRepository extends JpaRepository<Hoax, Integer>, JpaSpecificationExecutor<Hoax> {

	Page<Hoax> findByUser(User user,Pageable pageable);
	
	//Page<Hoax> findByIdLessThan(Integer id, Pageable pageable);

	//Page<Hoax> findByIdLessThanAndUser(Integer id, User user, Pageable pageable);
	
	//List<Hoax> findByIdGreaterThan(Integer id, Sort sort);
	
	//List<Hoax> findByIdGreaterThanAndUser(Integer id,User user, Sort sort);
	
	Integer countByIdGreaterThan(Integer id);
	
	Integer countByIdGreaterThanAndUser(Integer id, User user);
}
