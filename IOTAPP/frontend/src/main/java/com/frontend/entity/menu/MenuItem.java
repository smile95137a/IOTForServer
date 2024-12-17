package com.frontend.entity.menu;

import java.util.List;

import backend.entity.menu.MenuTabLayoutArea;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "menu_item")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MenuItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String uid;

	@Column
	private String richMenuId;

	@Column
	private String richMenuAliasId;

	@Column
	private String name;
	
	@Column
	private String chatBarText;

	@Column
	private Boolean isShow;
	
	@Column
	private String size;

	@Lob
	private String itemImg;
	
	@Column
	private String itemLayout;
	
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "menu_item＿id")
	private List<MenuArea> areas;

	
	private String tabLayoutPosition;
	
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "menu_item＿id")
	private List<MenuTabLayoutArea> tabLayoutAreas;
	
	@Column
	private int orderNum;


}
