package backend.entity.menu;

import backend.entity.role.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "menus")
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String menuName; // 菜单名称

    @Column(nullable = false)
    private String url; // 菜单路径

    @Column
    private String icon; // 菜单图标 (可选)

    @Column
    private Integer menuOrder; // 菜单排序

    @Column
    private String status; // 状态 (VISIBLE 或 HIDDEN)

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Menu parent; // 父菜单

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Menu> children; // 子菜单

    @ManyToMany(mappedBy = "menus")
    private Set<Role> roles; // 能访问此菜单的角色
}
