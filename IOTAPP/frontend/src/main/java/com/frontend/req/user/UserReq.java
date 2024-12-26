package com.frontend.req.user;

import java.io.Serializable;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserReq implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String uid;

	@NotNull
	private String username;

	@NotNull
	private String password;

	private String name;

	@NotNull
	private String email;


}
