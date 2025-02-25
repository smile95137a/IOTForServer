package com.frontend.req.user;

import com.frontend.enums.RoleName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserReq {
	
	private String uid;
	private String username;
	private String type;
    private String password;
    private String gender;
    private String name;
    private String email;
    private String phone;
    private String countryCode;
    private String verificationCode;
    private String anonymousId;
    private Set<RoleName> roleNames;
}


