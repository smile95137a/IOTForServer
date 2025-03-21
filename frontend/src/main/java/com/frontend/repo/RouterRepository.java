package com.frontend.repo;

import com.frontend.entity.router.Router;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RouterRepository extends JpaRepository<Router, Long> {

    List<Router> findByStoreId(Long storeId);

    int countByStoreId(Long storeId);
}
