package com.frontend.res.user;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFormat;
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
	
	private Long id;
	
	private String uid;

	private String username;

	private String password;
	
	private String countryCode;
	
	private String phoneNumber;

	private String name;

	private String email;

	private Integer point;

	private Integer balance; //餘額

	private Set<Role> roles = new HashSet<>();
	@JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
	private LocalDateTime createTime;

	private String createUserName;
	@JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
	private LocalDateTime updateTime;

	private String updateUserName;

	private LocalDateTime lastActiveTime;
	
	private Integer amount;

	private String userImg;

	private String gender;

	private Integer totalAmount;

	private String imgUrl;

	private Set<Long> roleIds = new HashSet<>();
	
	private String anonymousId;

	private String nickName;

	private BigDecimal totalConsumptionAmount;  // 总消費總金額
	private Integer totalConsumptionCount;  // 总消費筆數
	private BigDecimal totalDepositsAmount;  // 总儲值總金額
	private Integer totalDepositsCount;  // 总儲值筆數
}
