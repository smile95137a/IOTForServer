package com.frontend.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.frontend.entity.vendor.Vendor;
import com.frontend.res.vendor.VendorRes;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {

    Optional<Vendor> findByUid(String uid);

    @Query(value = "SELECT new com.frontend.res.vendor.VendorRes(s.id, s.uid, s.address, s.name, COUNT(pt.status)) " +
            "FROM Vendor v " +
            "JOIN v.stores s " +
            "JOIN s.poolTables pt " +
            "WHERE v.uid = :uid AND pt.status = 'AVAILABLE' " +
            "GROUP BY s.id, s.address, s.name, pt.status")
    Optional<List<VendorRes>> countAvailablePoolTables(@Param("uid") String uid);

}
