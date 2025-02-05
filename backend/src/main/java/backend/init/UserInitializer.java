package backend.init;

import backend.entity.role.Role;
import backend.entity.user.User;
import backend.enums.RoleName;
import backend.repo.RoleRepository;
import backend.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Component
@Order(0)  // 可調整初始化順序，確保其他初始化類別已經執行
public class UserInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        // 查找角色
        Optional<Role> adminRole = roleRepository.findByRoleName(RoleName.ROLE_ADMIN);
        Optional<Role> userRole = roleRepository.findByRoleName(RoleName.ROLE_USER);

        // 如果角色存在，創建用戶
        if (adminRole.isPresent() && userRole.isPresent()) {
            // 檢查是否已經創建過用戶，避免重複創建
            if (!userRepository.existsByUsername("user1")) {
                createUser("user1", "password1", "User One", "user1@example.com", adminRole.get(), userRole.get());
            }
            if (!userRepository.existsByUsername("user2")) {
                createUser("user2", "password2", "User Two", "user2@example.com", userRole.get());
            }
        } else {
            System.out.println("必須先創建角色 ROLE_ADMIN 和 ROLE_USER。");
        }
    }

    // 創建用戶並分配角色
    private void createUser(String username, String password, String name, String email, Role... roles) {
        Set<Role> roleSet = new HashSet<>();
        for (Role role : roles) {
            roleSet.add(role);
        }

        User user = User.builder()
                .username(username)
                .password(password)
                .name(name)
                .email(email)
                .roles(roleSet)
                .createTime(LocalDateTime.now())
                .createUserId(1L)  // 假設是由用戶ID 1創建
                .updateTime(LocalDateTime.now())
                .updateUserId(1L)
                .lastActiveTime(LocalDateTime.now())
                .build();

        userRepository.save(user);
    }
}
