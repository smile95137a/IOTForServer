package src.main.java.backend.entity.role;

import src.main.java.backend.entity.menu.Menu;
import src.main.java.backend.enums.RoleName;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "roles")
@ToString(exclude = {"menus"})
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column
    private RoleName roleName;

    @Column
    private String description; // 角色描述

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_menu",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "menu_id")
    )
    @JsonManagedReference // 防止序列化 role 相關的菜單循環引用
    private Set<Menu> menus = new HashSet<>();

}
