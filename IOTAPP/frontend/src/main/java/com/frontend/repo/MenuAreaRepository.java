package com.frontend.repo;


import backend.entity.menu.MenuArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface MenuAreaRepository extends JpaRepository<MenuArea, Long> {
	
	
	Optional<MenuArea> findByuid(String uid);
}