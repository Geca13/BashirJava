package com.example.bashir.hoax;

import com.example.bashir.user.vm.UserVM;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HoaxVm {
	
	private Integer id;
	
	private String content;
	
	private long date;
	
	private UserVM user;
	
	public HoaxVm(Hoax hoax) {
		this.setId(hoax.getId());
		this.setContent(hoax.getContent());
		this.setDate(hoax.getTimestamp().getTime());
		this.setUser(new UserVM(hoax.getUser()));
	}

}
