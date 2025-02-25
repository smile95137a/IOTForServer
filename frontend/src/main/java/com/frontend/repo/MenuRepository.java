package com.frontend.repo;


import com.frontend.entity.menu.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
    // 根据 roleId 查询菜单列表
    List<Menu> findByRolesId(Long roleId);

    // 判断菜单名称和 URL 是否存在
    boolean existsByMenuNameAndUrl(String menuName, String url);
}

