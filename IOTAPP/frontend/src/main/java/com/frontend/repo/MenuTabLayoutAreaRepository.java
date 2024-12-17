package com.frontend.repo;


import backend.entity.menu.MenuTabLayoutArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface MenuTabLayoutAreaRepository extends JpaRepository<MenuTabLayoutArea, Long> {
	
	
	Optional<MenuTabLayoutArea> findByuid(String uid);
}