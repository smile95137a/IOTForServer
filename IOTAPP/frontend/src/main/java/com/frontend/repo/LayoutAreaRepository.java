package com.frontend.repo;

import backend.entity.layout.LayoutArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface LayoutAreaRepository extends JpaRepository<LayoutArea, Long> {
}