package com.example.bashir.hoax;

import com.example.bashir.file.FileAttachmentVm;
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
	
	private FileAttachmentVm attachment;
	
	public HoaxVm(Hoax hoax) {
		this.setId(hoax.getId());
		this.setContent(hoax.getContent());
		this.setDate(hoax.getTimestamp().getTime());
		this.setUser(new UserVM(hoax.getUser()));
		if(hoax.getAttachment() != null) {
			this.setAttachment(new FileAttachmentVm(hoax.getAttachment()));
		}
	}

}
