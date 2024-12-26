package com.frontend.req.menu;

import java.util.Date;
import java.util.List;

import com.frontend.req.menu.MenuItemReq;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MenuReq {
	private String uid;
	private String menuName;
	private String menuDesc;
	
	 @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm", timezone = "Asia/Taipei")
    private Date startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm", timezone = "Asia/Taipei")
    private Date endDate;
	private String channeluid;
	private Boolean isShow;
	private Boolean isPublish;
	private List<MenuItemReq> items;
}
