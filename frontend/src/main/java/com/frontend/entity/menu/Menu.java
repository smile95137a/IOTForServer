package com.frontend.entity.menu;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.frontend.entity.role.Role;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "menus")
@ToString(exclude = {"roles", "parent", "children"}) // Menu類
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

    @ManyToMany(mappedBy = "menus", fetch = FetchType.EAGER)
    @JsonBackReference
    private Set<Role> roles;
}
