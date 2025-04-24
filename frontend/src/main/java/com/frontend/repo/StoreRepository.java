package com.frontend.repo;

import com.frontend.entity.vendor.Vendor;
import com.frontend.res.store.StorePricingScheduleRes;
import org.springframework.data.jpa.repository.JpaRepository;
import com.frontend.entity.store.Store;
import com.frontend.res.store.StoreRes;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

	Optional<Store> findByUid(String uid);

	// 新增Repository方法，用于获取商店的定价时间表
	@Query("SELECT s FROM Store s " +
			"LEFT JOIN FETCH s.poolTables pt " +
			"LEFT JOIN FETCH s.pricingSchedules ps " +
			"LEFT JOIN FETCH ps.timeSlots ts " +
			"WHERE s.uid = :uid")
	List<Store> findStoresWithPoolTableCountsByUid(@Param("uid") String uid);




	void deleteByUid(String uid);

	List<Store> findByVendor(Vendor vendor);

    List<Store> findByVendorId(Long vendorId);

	@Query("SELECT DISTINCT s FROM Store s " +
			"LEFT JOIN FETCH s.poolTables pt " +
			"LEFT JOIN FETCH s.pricingSchedules ps " +
			"WHERE s.uid = :uid")
	Optional<List<Store>> getStoresWithPricingByUid(@Param("uid") String uid, @Param("status") String status);

	List<Store> findByUserId(Long userId);
}
