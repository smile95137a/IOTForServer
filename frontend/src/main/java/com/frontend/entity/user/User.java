package com.frontend.entity.user;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
	private String phoneNumber;

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

	@Column
	private String userImg;

	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
	@Builder.Default
	@JsonIgnore
	private Set<Role> roles = new HashSet<>();


	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "vendor_id")
	@JsonBackReference
	@JsonIgnore
	private Vendor vendor;


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
