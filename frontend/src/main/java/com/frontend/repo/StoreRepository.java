package com.frontend.repo;

import com.frontend.entity.vendor.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import com.frontend.entity.store.Store;
import com.frontend.res.store.StoreRes;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

	Optional<Store> findByUid(String uid);

	@Query("SELECT new com.frontend.res.store.StoreRes(s.id, s.uid, s.address, s.name, "
			+ "COUNT(CASE WHEN pt.status = :status THEN 1 ELSE NULL END), "
			+ "COUNT(CASE WHEN pt.status != :status THEN 1 ELSE NULL END), "
			+ "s.lat, s.lon, "
			+ "s.deposit , s.imgUrl , "
			+ "null , s.hint , s.contactPhone) " // 这里传递 null 给 pricingSchedules
			+ "FROM Store s "
			+ "LEFT JOIN s.poolTables pt "
			+ "WHERE s.uid = :uid "
			+ "GROUP BY s.id, s.uid, s.address, s.name, s.lat, s.lon, s.deposit , s.imgUrl ")
	Optional<List<StoreRes>> countAvailableAndInUseByUid(
			@Param("uid") String uid,
			@Param("status") String status);




	void deleteByUid(String uid);

	List<Store> findByVendor(Vendor vendor);

    List<Store> findByVendorId(Long vendorId);
}
