package com.frontend.req.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserReq {

	private String username;
	private String password;
	private String natId;
	private String nickname;
	private String name;
	private String email;
	private Integer phoneNumber;
	private String city;
	private String area;
	private String address;
	private String addressName;
	private String lineId;
	private String invoiceInfo;
	private String invoiceInfoEmail;

	private String zipCode;
	private String vehicle;
}
