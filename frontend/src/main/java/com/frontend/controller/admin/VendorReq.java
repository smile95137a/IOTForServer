package com.frontend.controller.admin;

import java.util.Set;

import com.frontend.entity.store.Store;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VendorReq {

	private String name;
	private String contactInfo;
	private Long userId;
	private String companyAddress;
	private String phoneNumber;
	private String email;
	private String telephoneNumber;
	private String address;
	private String companyName;

}
