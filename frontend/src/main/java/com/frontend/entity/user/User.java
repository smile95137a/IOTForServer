package com.frontend.entity.user;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.frontend.entity.role.Role;
import com.frontend.entity.store.Store;
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
	private String nickName;

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

	@OneToOne(mappedBy = "user")
	@JsonManagedReference("userReference")
	private Store store;


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
	@JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
	private LocalDateTime createTime;

	@Column
	private Long createUserId;

	@Column
	@JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
	private LocalDateTime updateTime;

	@Column
	private Long updateUserId;

	@Column
	private LocalDateTime lastActiveTime;

	@Column
	private Integer amount;

	@Column
	private Integer point;

	@Column
	private Integer balance; //餘額

	@Column
	private Integer totalAmount;
}
