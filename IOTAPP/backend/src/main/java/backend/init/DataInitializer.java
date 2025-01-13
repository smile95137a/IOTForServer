package backend.init;

import backend.entity.menu.Menu;
import backend.entity.role.Role;
import backend.enums.RoleName;
import backend.repo.MenuRepository;
import backend.repo.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.List;

@Component
@Order(2)
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Override
    public void run(String... args) throws Exception {
        // Retrieve menu items by their IDs
        List<Menu> menus = menuRepository.findAllById(List.of(1L, 2L, 3L, 4L, 5L, 6L));

        // Create roles and assign menus if they do not exist
        createRoleIfNotExist(RoleName.ROLE_ADMIN, "最高權限者", menus.subList(0, 6)); // RoleAdmin
        createRoleIfNotExist(RoleName.ROLE_MANUFACTURER, "廠商權限", menus.subList(0, 3)); // RoleManufacturer
        createRoleIfNotExist(RoleName.ROLE_USER, "一般會員", menus.subList(0, 2)); // RoleUser
        createRoleIfNotExist(RoleName.ROLE_BLACKLIST, "黑名單", menus.subList(0, 1)); // RoleBlacklist
    }

    private void createRoleIfNotExist(RoleName roleName, String description, List<Menu> menus) {
        Optional<Role> roleOptional = roleRepository.findByRoleName(roleName);
        if (roleOptional.isEmpty()) {
            // If the role does not exist, create a new role and assign menus
            Role newRole = new Role();
            newRole.setRoleName(roleName);
            newRole.setDescription(description);
            newRole.getMenus().addAll(menus);  // Add all valid menus

            roleRepository.save(newRole);  // Save the new role
        } else {
            // If the role exists, ensure it gets the correct menu associations
            Role role = roleOptional.get();
            role.getMenus().clear();  // Clear existing menus before re-adding them
            role.getMenus().addAll(menus);  // Add new menus
            roleRepository.save(role);  // Save the updated role
        }
    }
}
