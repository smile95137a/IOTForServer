package com.frontend.res.menu;

import java.util.Date;
import java.util.List;

import com.frontend.res.menu.MenuItemRes;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MenuRes {
    private String uid;
    private String menuName;
    private String menuDesc;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm", timezone = "Asia/Taipei")
    private Date startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm", timezone = "Asia/Taipei")
    private Date endDate;

    private String channeluid;
    private Boolean isEnable;	
    private Boolean isShow;
    private List<MenuItemRes> items;
    private Date createDate;
    private String createUserName;
    private Date updateDate;
    private String updateUserName;
}
