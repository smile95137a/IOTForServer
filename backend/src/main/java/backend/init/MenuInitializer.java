package src.main.java.backend.init;

import src.main.java.backend.entity.menu.Menu;
import src.main.java.backend.repo.MenuRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.Arrays;
import java.util.List;

@Configuration
@Order(1)
public class MenuInitializer {

    @Bean
    public CommandLineRunner initMenuData(MenuRepository menuRepository) {
        return args -> {
            // Define the menu items to be initialized
            List<Menu> menus = Arrays.asList(
                new Menu(null, "經營報表", "/report", null, 1, "VISIBLE", null, null, null),
                new Menu(null, "會員管理", "/member", null, 2, "VISIBLE", null, null, null),
                new Menu(null, "優惠方案設置", "/discount", null, 3, "VISIBLE", null, null, null),
                new Menu(null, "桌台管理", "/table", null, 4, "VISIBLE", null, null, null),
                new Menu(null, "設備管理", "/device", null, 5, "VISIBLE", null, null, null),
                new Menu(null, "我的門店", "/store", null, 6, "VISIBLE", null, null, null)
            );

            // Save each menu item if it does not already exist
            for (Menu menu : menus) {
                if (!menuRepository.existsByMenuNameAndUrl(menu.getMenuName(), menu.getUrl())) {
                    menuRepository.save(menu);
                }
            }
        };
    }
}
