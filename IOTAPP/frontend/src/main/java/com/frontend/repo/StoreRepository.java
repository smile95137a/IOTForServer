package src.main.java.com.frontend.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import src.main.java.com.frontend.entity.store.Store;
import src.main.java.com.frontend.res.store.StoreRes;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    Optional<Store> findByUid(String uid);

    @Query(value = "SELECT new src.main.java.com.frontend.res.store.StoreRes(s.id, s.uid, s.address, s.name, " +
            "COUNT(CASE WHEN pt.status = :status THEN 1 ELSE NULL END), " +
            "COUNT(CASE WHEN pt.status != :status THEN 1 ELSE NULL END)) " +
            "FROM Store s " +
            "JOIN s.poolTables pt " +
            "WHERE s.uid = :uid " +
            "GROUP BY s.id, s.uid, s.address, s.name")
    Optional<List<StoreRes>> countAvailableAndInUseByUid(@Param("uid") String uid, @Param("status") String status);


}
