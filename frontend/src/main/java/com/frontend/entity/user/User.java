package com.frontend.entity.user;

import com.frontend.entity.role.Role;
import com.frontend.entity.vendor.Vendor;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false)
	private String gender;

	@Column
	private String uid;

	@Column
	private String username;

	@Column
	private Integer phoneNumber;

	@Column
	private String password;

	@Column
	private String name;
	@Column
	private String countryCode;
	@Column
	private String verificationCode;
	@Column
	private String anonymousId;

	@Column
	private String email;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
	@Builder.Default
	private Set<Role> roles = new HashSet<>();

	@ManyToOne
	@JoinColumn(name = "vendor_id")  // 外鍵指向廠商
	private Vendor vendor;  // 用戶對應的廠商

	@Column
	private LocalDateTime createTime;

	@Column
	private Long createUserId;

	@Column
	private LocalDateTime updateTime;

	@Column
	private Long updateUserId;

	@Column
	private LocalDateTime lastActiveTime;

	@Column
	private Integer amount;

	@Column
	private Integer totalAmount;
}
