package com.frontend.res.menu;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MenuItemRes {
	private Long id;
	private String uid;
	private String richMenuId;
	private String richMenuAliasId;
	private String name;
	private String chatBarText;
	private Boolean isShow;
	private String size;
	private String itemImg;
	private String itemLayout;
	private List<MenuAreaRes> areas;
	private String tabLayoutPosition;
	private List<com.mo.app.res.menu.MenuTabLayoutAreaRes> tabLayoutAreas;
	private LocalDateTime createDate;
	private String createUserName;
	private LocalDateTime updateDate;
	private String updateUserName;
	private int orderNum;
}


