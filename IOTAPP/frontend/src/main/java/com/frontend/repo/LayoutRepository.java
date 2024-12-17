package com.frontend.repo;

import backend.entity.layout.Layout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface LayoutRepository extends JpaRepository<Layout, Long> {
}