package com.frontend.res.user;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.frontend.entity.role.Role;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRes implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@JsonIgnore
	private Long id;
	
	private String uid;

	private String username;

	private String password;
	
	private String countryCode;
	
	private String phoneNumber;

	private String name;

	private String email;

	private Set<Role> roles = new HashSet<>();
	
	private LocalDateTime createTime;

	private String createUserName;

	private LocalDateTime updateTime;

	private String updateUserName;

	private LocalDateTime lastActiveTime;
	
	private Integer amount;

	private Integer totalAmount;

	private Set<Long> roleIds = new HashSet<>();
}
