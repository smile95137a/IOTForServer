package com.frontend.req.menu;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MenuItemReq {

	private String uid;
	private String richMenuAliasId;
	private String name;
	private String chatBarText;
	private Boolean isShow;
	private String itemImg;
	private String itemLayout;
	private String size;
	private List<MenuAreaReq> areas;
	private String tabLayoutPosition;
	private List<com.mo.app.req.menu.MenuTabLayoutAreaReq> tabLayoutAreas;
	private int orderNum;
	
}
