package src.main.java.com.frontend.res.menu;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MenuListRes {
	List<com.mo.app.res.menu.MenuRes> activeMenus;
	List<com.mo.app.res.menu.MenuRes> expiredMenus;
}
