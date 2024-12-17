package com.frontend.entity.menu;

import java.util.Date;
import java.util.List;

import backend.entity.menu.MenuItem;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "menu")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Menu {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String uid;

	@Column
	private String menuName;

	@Column
	private String menuDesc;

	@Column
	private Date startDate;

	@Column
	private Date endDate;
	
	@Column
	private Long channelId;

	@Column
	private Boolean isEnable;
	
	@Column
	private Boolean isShow;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "menu_id")
	private List<MenuItem> items;

	@Column
	private Date createDate;

	@Column
	private Long createUserId;
	
	@Column
	private Date updateDate;
	
	@Column
	private Long updateUserId;

}
