package com.frontend.controller.admin;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.frontend.entity.news.News;
import com.frontend.entity.store.StorePricingSchedule;
import com.frontend.repo.StorePricingScheduleRepository;
import com.frontend.req.store.StorePricingScheduleReq;
import com.frontend.res.store.AdminStoreRes;
import com.frontend.res.store.StoreRes;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.frontend.entity.store.Store;
import com.frontend.repo.StoreRepository;
import com.frontend.utils.RandomUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AdminStoreService {

	private final StoreRepository storeRepository;

	@Autowired
	private StorePricingScheduleRepository storePricingScheduleRepository;

	// Create a new store
	public Store createStore(StoreReq storeReq, Long userId) {
		// 转换并保存 Store 实体
		Store store = convertToEntity(storeReq);
		store.setUid(RandomUtils.genRandom(24)); // 生成唯一 UID
		store.setCreateTime(LocalDateTime.now());
		store.setCreateUserId(userId);
		store.setImgUrl("");
		// 保存 Store
		Store savedStore = storeRepository.save(store);

		// 处理并保存 StorePricingSchedule
		if (storeReq.getPricingSchedules() != null && !storeReq.getPricingSchedules().isEmpty()) {
			Set<StorePricingSchedule> schedules = storeReq.getPricingSchedules().stream().map(scheduleReq -> {
				StorePricingSchedule schedule = new StorePricingSchedule();
				schedule.setDayOfWeek(scheduleReq.getDayOfWeek());
				schedule.setRegularStartTime(scheduleReq.getRegularStartTime());
				schedule.setRegularEndTime(scheduleReq.getRegularEndTime());
				schedule.setRegularRate(scheduleReq.getRegularRate());
				schedule.setDiscountStartTime(scheduleReq.getDiscountStartTime());
				schedule.setDiscountEndTime(scheduleReq.getDiscountEndTime());
				schedule.setDiscountRate(scheduleReq.getDiscountRate());
				schedule.setStore(savedStore); // 设置关联关系
				return schedule;
			}).collect(Collectors.toSet());

			storePricingScheduleRepository.saveAll(schedules); // 保存所有定价计划
		}

		return savedStore;
	}


	private Store convertToEntity(StoreReq req) {
		Store store = new Store();
		store.setName(req.getName());
		store.setAddress(req.getAddress());
		store.setLat(req.getLat());
		store.setLon(req.getLon());
		store.setDeposit(req.getDeposit());
		store.setImgUrl(req.getImgUrl());
		store.setHint(req.getHint());
		store.setContactPhone(req.getContactPhone());
		if (req.getVendor() != null) {
			store.setVendor(req.getVendor());
		}
		if (req.getPoolTables() != null) {
			store.setPoolTables(req.getPoolTables());
		}

		return store;
	}
	public List<AdminStoreRes> getAllStores() {
		List<Store> stores = storeRepository.findAll();
		return stores.stream()
				.map(this::convertToAdminStoreRes)
				.collect(Collectors.toList());
	}

	// Retrieve a store by ID
	public Optional<AdminStoreRes> getStoreById(String uid) {
		Optional<Store> store = storeRepository.findByUid(uid);
		if (store.isPresent()) {
			AdminStoreRes adminStoreRes = convertToAdminStoreRes(store.get());
			return Optional.of(adminStoreRes);
		}
		return Optional.empty();
	}

	private AdminStoreRes convertToAdminStoreRes(Store store) {
		AdminStoreRes.AdminStoreResBuilder builder = AdminStoreRes.builder()
				.id(store.getId())
				.uid(store.getUid())
				.name(store.getName())
				.address(store.getAddress())
				.lat(store.getLat())
				.lon(store.getLon())
				.deposit(store.getDeposit())
				.vendor(store.getVendor())
				.poolTables(store.getPoolTables())
				.pricingSchedules(store.getPricingSchedules())
						.hint(store.getHint())
						.contactPhone(store.getContactPhone());


		// 只在 poolTables 不为 null 时设置 poolTables
		if (store.getPoolTables() != null) {
			builder.poolTables(store.getPoolTables());
		}

		return builder.build();
	}



	// Update a store
	public Store updateStore(String uid, StoreReq storeReq, Long id) {
		return storeRepository.findByUid(uid).map(store -> {
			store.setName(storeReq.getName());
			store.setAddress(storeReq.getAddress());
			store.setLat(storeReq.getLat());
			store.setLon(storeReq.getLon());
			store.setDeposit(storeReq.getDeposit());
			store.setHint(storeReq.getHint());
			store.setContactPhone(storeReq.getContactPhone());
			// 设备和桌台
			if (storeReq.getVendor() != null) {
				store.setVendor(storeReq.getVendor());
			}
			if (storeReq.getPoolTables() != null) {
				store.setPoolTables(storeReq.getPoolTables());
			}

			// **修正 pricingSchedules 逻辑**
			if (storeReq.getPricingSchedules() != null) {
				// **清空原集合，避免 orphanRemoval 触发异常**
				store.getPricingSchedules().clear();

				for (StorePricingScheduleReq scheduleReq : storeReq.getPricingSchedules()) {
					StorePricingSchedule schedule = new StorePricingSchedule();
					schedule.setDayOfWeek(scheduleReq.getDayOfWeek());
					schedule.setRegularStartTime(scheduleReq.getRegularStartTime());
					schedule.setRegularEndTime(scheduleReq.getRegularEndTime());
					schedule.setRegularRate(scheduleReq.getRegularRate());
					schedule.setDiscountStartTime(scheduleReq.getDiscountStartTime());
					schedule.setDiscountEndTime(scheduleReq.getDiscountEndTime());
					schedule.setDiscountRate(scheduleReq.getDiscountRate());
					schedule.setStore(store); // 重要：确保 store 仍然关联
					store.getPricingSchedules().add(schedule);
				}
			}

			// **更新修改时间 & 用户**
			store.setUpdateTime(LocalDateTime.now());
			store.setUpdateUserId(id);

			return storeRepository.save(store);
		}).orElseThrow(() -> new RuntimeException("Store not found with uid: " + uid));
	}




	// Delete a store
	public void deleteStore(String uid) {
		storeRepository.deleteByUid(uid);
	}

	public void uploadProductImg(Long id, String uploadedFilePath) {
		Store store = storeRepository.findById(id).orElseThrow(() -> new RuntimeException("News not found with id: " + id));
		if(store != null){
			store.setImgUrl(uploadedFilePath);
			storeRepository.save(store);
		}
	}

	public List<StoreRes> getStoresByVendorId(Long vendorId) {
		List<Store> stores = storeRepository.findByVendorId(vendorId);
		return stores.stream().map(this::convertToRes).collect(Collectors.toList());
	}

	private StoreRes convertToRes(Store store) {
		StoreRes res = new StoreRes();
		BeanUtils.copyProperties(store, res);
		return res;
	}

}
