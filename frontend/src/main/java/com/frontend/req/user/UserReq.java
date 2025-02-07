package com.frontend.req.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserReq {
	private String type;
    private String password;
    private String gender;
    private String name;
    private String email;
    private String phone;
    private String countryCode;
    private String verificationCode;
    private String anonymousId;
}


