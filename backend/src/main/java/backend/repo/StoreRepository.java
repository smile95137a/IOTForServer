package src.main.java.backend.repo;

import src.main.java.backend.entity.store.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    Optional<Store> findByUid(String uid);

    void deleteByUid(String uid);


}
