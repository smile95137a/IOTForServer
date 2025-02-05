package src.main.java.backend.entity.user;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import src.main.java.backend.entity.vendor.Vendor;  // 引入廠商類別
import src.main.java.backend.entity.role.Role;
import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

	@Column
	private String uid;

	@Column
	private String username;

	@Column
	private String nickname;

	@Column
	private Integer phoneNumber;

	@Column
	private String lineId;

	@Column
	private String password;

	@Column
	private String name;

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
	private BigDecimal totalAmount;
}
