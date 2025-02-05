//package src.main.java.backend.mapper;
//
//import java.net.URI;
//import java.time.LocalDateTime;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.stream.Collectors;
//
//import src.main.java.backend.entity.menu.Menu;
//import src.main.java.backend.entity.menu.MenuArea;
//import src.main.java.backend.entity.menu.MenuItem;
//import src.main.java.backend.entity.menu.MenuTabLayoutArea;
//import src.main.java.backend.enums.AreaType;
//import src.main.java.backend.req.menu.MenuAreaReq;
//import src.main.java.backend.req.menu.MenuItemReq;
//import src.main.java.backend.req.menu.MenuReq;
//import src.main.java.backend.req.menu.MenuTabLayoutAreaReq;
//import src.main.java.backend.res.menu.MenuAreaRes;
//import src.main.java.backend.res.menu.MenuItemRes;
//import src.main.java.backend.res.menu.MenuRes;
//import src.main.java.backend.res.menu.MenuTabLayoutAreaRes;
//import src.main.java.backend.service.UserService;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import com.mo.app.utils.JsonUtils;
//import com.mo.app.utils.SecurityUtils;
//
//import lombok.RequiredArgsConstructor;
//
//@Component
//@RequiredArgsConstructor
//public class MenuMapper {
//
//	private final RichMenuService richMenuService;
//
//	private final ChannelService channelService;
//
//	private final UserService userService;
//
//	@Value("${frontend.domain}")
//	private String frontendDomain;
//
//	public List<RichMenuArea> mapAreas(List<MenuArea> areas) {
//		return areas.stream().map(area -> {
//
//			if (area.getType() == null) {
//				return null;
//			}
//
//			var callBackData = new HashMap<String, String>() {
//				{
//					put("action", "btn");
//					put("id", area.getUid());
//				}
//			};
//			var data = JsonUtils.objectToJson(callBackData);
//			switch (area.getType()) {
//			case SWITCH:
//				return richMenuService.makeRichMenuSwitchAction(area.getX(), area.getY(), area.getWidth(),
//						area.getHeight(), area.getLabel(), data, area.getSwitchRichMenu());
//			case MSG:
//
//				return richMenuService.makePostbackAction(area.getX(), area.getY(), area.getWidth(), area.getHeight(),
//						area.getLabel(), data, area.getText());
//			case URI:
//					return richMenuService.makeURIAction(area.getX(), area.getY(), area.getWidth(), area.getHeight(),
//							area.getLabel(), URI.create(area.getText()));
//			default:
//				return null;
//			}
//		}).collect(Collectors.toList());
//	}
//
//	public List<RichMenuArea> mapTabLayoutAreas(List<MenuTabLayoutArea> areas) {
//		return areas.stream().map(area -> {
//			if (area.getType() == null) {
//				return null;
//			}
//			var callBackData = new HashMap<String, String>() {
//				{
//					put("action", "btn");
//					put("id", area.getUid());
//				}
//			};
//			var data = JsonUtils.objectToJson(callBackData);
//			switch (area.getType()) {
//			case SWITCH:
//				return richMenuService.makeRichMenuSwitchAction(area.getX(), area.getY(), area.getWidth(),
//						area.getHeight(), area.getLabel(), data, area.getSwitchRichMenu());
//			case MSG:
//
//				return richMenuService.makePostbackAction(area.getX(), area.getY(), area.getWidth(), area.getHeight(),
//						area.getLabel(), data, area.getText());
//			case URI:
//					return richMenuService.makeURIAction(area.getX(), area.getY(), area.getWidth(), area.getHeight(),
//							area.getLabel(), URI.create(area.getText()));
//			default:
//				return null;
//			}
//		}).collect(Collectors.toList());
//	}
//
//	public RichMenu mapRichMenu(MenuItem menuItem, List<RichMenuArea> areas) {
//		return RichMenu.builder().name(menuItem.getName()).chatBarText(menuItem.getChatBarText()).areas(areas)
//				.selected(menuItem.getIsShow())
//				.size(menuItem.getSize().equals("FULL") ? RichMenuSize.FULL : RichMenuSize.HALF).build();
//	}
//
//	public Menu mapTemplateGroup(MenuReq req) {
//		var now = new Date();
//		var userId = SecurityUtils.getSecurityUser().getId();
//
//		return Menu.builder().uid(req.getUid()).menuName(req.getMenuName()).menuDesc(req.getMenuDesc())
//				.startDate(req.getStartDate()).endDate(req.getEndDate()).isShow(req.getIsShow()).createDate(now).createUserId(userId)
//				.updateDate(now).updateUserId(userId).isEnable(false).build();
//	}
//
//	public MenuItem mapMenuItem(MenuItemReq req) {
//		var now = LocalDateTime.now();
//		var userId = SecurityUtils.getSecurityUser().getId();
//		var areas = req.getAreas().stream().map(this::mapMenuArea).collect(Collectors.toList());
//		var tabLayoutAreas = req.getTabLayoutAreas().stream().map(this::mapMenuTabLayoutArea)
//				.collect(Collectors.toList());
//		return MenuItem.builder().uid(req.getUid()).richMenuAliasId(req.getRichMenuAliasId()).name(req.getName())
//				.chatBarText(req.getChatBarText()).isShow(req.getIsShow()).itemImg(req.getItemImg())
//				.itemLayout(req.getItemLayout()).size(req.getSize()).areas(areas)
//				.tabLayoutPosition(req.getTabLayoutPosition()).tabLayoutAreas(tabLayoutAreas)
//				.orderNum(req.getOrderNum()).build();
//
//	}
//
//	public MenuArea mapMenuArea(MenuAreaReq req) {
//
//		var type = StringUtils.isBlank(req.getType()) ? null : AreaType.valueOf(req.getType().toUpperCase());
//		return MenuArea.builder().uid(req.getUid()).layoutUid(req.getLayoutUid()).isTabArea(req.getIsTabArea())
//				.x(req.getX()).y(req.getY()).width(req.getWidth()).height(req.getHeight()).label(req.getLabel())
//				.type(type).text(req.getText()).switchRichMenu(req.getSwitchRichMenu()).orderNum(req.getOrderNum())
//				.clickCount(0).build();
//	}
//
//	public MenuTabLayoutArea mapMenuTabLayoutArea(MenuTabLayoutAreaReq req) {
//		var type = StringUtils.isBlank(req.getType()) ? null : AreaType.valueOf(req.getType().toUpperCase());
//		return MenuTabLayoutArea.builder().uid(req.getUid()).layoutUid(req.getLayoutUid()).isTabArea(req.getIsTabArea())
//				.x(req.getX()).y(req.getY()).width(req.getWidth()).height(req.getHeight()).label(req.getLabel())
//				.type(type).text(req.getText()).switchRichMenu(req.getSwitchRichMenu()).orderNum(req.getOrderNum())
//				.clickCount(0).build();
//	}
//
//	public MenuRes mapMenuEntityToRes(Menu menu) {
//
//		var cEntity = channelService.getChannelByid(menu.getChannelId());
//		var user = userService.getUserById(menu.getCreateUserId());
//
//		var res = MenuRes.builder().uid(menu.getUid()).menuName(menu.getMenuName()).menuDesc(menu.getMenuDesc())
//				.startDate(menu.getStartDate()).endDate(menu.getEndDate()).isShow(menu.getIsShow()).createDate(menu.getCreateDate())
//				.createUserName(user.getName()).updateDate(menu.getUpdateDate())
//				.items(mapMenuItemsToRes(menu.getItems())).isEnable(menu.getIsEnable()).channeluid(cEntity.getUid())
//				.build();
//
//		if (menu.getUpdateUserId() != null) {
//			var uue = userService.getUserById(menu.getUpdateUserId());
//			res.setUpdateUserName(uue.getName());
//		}
//
//		return res;
//	}
//
//	public MenuItemRes mapMenuItemToRes(MenuItem menuItem) {
//
//		var res = MenuItemRes.builder().uid(menuItem.getUid()).richMenuId(menuItem.getRichMenuId())
//				.richMenuAliasId(menuItem.getRichMenuAliasId()).chatBarText(menuItem.getChatBarText())
//				.name(menuItem.getName()).isShow(menuItem.getIsShow()).itemImg(menuItem.getItemImg())
//				.itemLayout(menuItem.getItemLayout()).size(menuItem.getSize())
//				.areas(mapMenuAreasToRes(menuItem.getAreas())).tabLayoutPosition(menuItem.getTabLayoutPosition())
//				.tabLayoutAreas(mapMenuTabLayoutAreasToRes(menuItem.getTabLayoutAreas()))
//				.orderNum(menuItem.getOrderNum()).build();
//		return res;
//	}
//
//	public MenuAreaRes mapMenuAreaToRes(MenuArea menuArea) {
//
//		var type = menuArea.getType() == null ? null : menuArea.getType().name();
//		return MenuAreaRes.builder().uid(menuArea.getUid()).layoutUid(menuArea.getLayoutUid())
//				.isTabArea(menuArea.getIsTabArea()).x(menuArea.getX()).y(menuArea.getY()).width(menuArea.getWidth())
//				.height(menuArea.getHeight()).label(menuArea.getLabel()).type(type).text(menuArea.getText())
//				.switchRichMenu(menuArea.getSwitchRichMenu()).clickCount(menuArea.getClickCount())
//				.orderNum(menuArea.getOrderNum()).build();
//	}
//
//	public MenuTabLayoutAreaRes mapMenuTabLayoutAreaToRes(MenuTabLayoutArea menuTabLayoutArea) {
//		var type = menuTabLayoutArea.getType() == null ? null : menuTabLayoutArea.getType().name();
//		return MenuTabLayoutAreaRes.builder().uid(menuTabLayoutArea.getUid())
//				.layoutUid(menuTabLayoutArea.getLayoutUid()).isTabArea(menuTabLayoutArea.getIsTabArea())
//				.x(menuTabLayoutArea.getX()).y(menuTabLayoutArea.getY()).width(menuTabLayoutArea.getWidth())
//				.height(menuTabLayoutArea.getHeight()).label(menuTabLayoutArea.getLabel()).type(type)
//				.text(menuTabLayoutArea.getText()).switchRichMenu(menuTabLayoutArea.getSwitchRichMenu())
//				.clickCount(menuTabLayoutArea.getClickCount()).orderNum(menuTabLayoutArea.getOrderNum()).build();
//	}
//
//	private List<MenuItemRes> mapMenuItemsToRes(List<MenuItem> menuItems) {
//		return menuItems.stream().map(this::mapMenuItemToRes).collect(Collectors.toList());
//	}
//
//	private List<MenuAreaRes> mapMenuAreasToRes(List<MenuArea> menuAreas) {
//		return menuAreas.stream().map(this::mapMenuAreaToRes).collect(Collectors.toList());
//	}
//
//	private List<MenuTabLayoutAreaRes> mapMenuTabLayoutAreasToRes(List<MenuTabLayoutArea> menuTabLayoutAreas) {
//		return menuTabLayoutAreas.stream().map(this::mapMenuTabLayoutAreaToRes).collect(Collectors.toList());
//	}
//
//}
