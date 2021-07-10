package com.example.bashir.hoax;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.bashir.user.User;

@Repository
public interface HoaxRepository extends JpaRepository<Hoax, Integer> {

	Page<Hoax> findByUser(User user,Pageable pageable);
	
	Page<Hoax> findByIdLessThan(Integer id, Pageable pageable);

}
