package com.frontend.repo;

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

	@Query(value = "SELECT new com.frontend.res.store.StoreRes(s.id, s.uid, s.address, s.name, "
			+ "COUNT(CASE WHEN pt.status = :status THEN 1 ELSE NULL END), " // availablesCount
			+ "COUNT(CASE WHEN pt.status != :status THEN 1 ELSE NULL END), " // inusesCount
			+ "s.lat, s.lon, "
			+ "s.deposit, "
			+ "COALESCE(MAX(ps.regularRate), 0), " // regularRate
			+ "COALESCE(MAX(ps.discountRate), 0), " // discountRate
			+ "COALESCE(MAX(ps.regularStartTime || '-' || ps.regularEndTime), '') " // regularTimeRange
			+ ") "
			+ "FROM Store s "
			+ "LEFT JOIN s.poolTables pt "
			+ "LEFT JOIN s.pricingSchedules ps " // 連接 StorePricingSchedule
			+ "WHERE s.uid = :uid "
			+ "GROUP BY s.id, s.uid, s.address, s.name, s.lat, s.lon, s.deposit")
	Optional<List<StoreRes>> countAvailableAndInUseByUid(
			@Param("uid") String uid,
			@Param("status") String status);

	void deleteByUid(String uid);
}
