package com.frontend.controller.admin;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.frontend.entity.news.News;
import com.frontend.entity.store.StorePricingSchedule;
import com.frontend.entity.store.TimeSlot;
import com.frontend.repo.StorePricingScheduleRepository;
import com.frontend.req.store.StorePricingScheduleReq;
import com.frontend.req.store.TimeSlotReq;
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
		store.setImgUrl(storeReq.getImgUrl() != null ? storeReq.getImgUrl() : ""); // 确保图片 URL
		// 保存 Store
		Store savedStore = storeRepository.save(store);

		// 处理并保存 StorePricingSchedule
		if (storeReq.getPricingSchedules() != null && !storeReq.getPricingSchedules().isEmpty()) {
			Set<StorePricingSchedule> schedules = storeReq.getPricingSchedules().stream().map(scheduleReq -> {
				StorePricingSchedule schedule = new StorePricingSchedule();
				schedule.setDayOfWeek(scheduleReq.getDayOfWeek());
				schedule.setStore(savedStore); // 设置关联关系
				schedule.setRegularRate(scheduleReq.getRegularRate()); // 设置普通时段价格
				schedule.setDiscountRate(scheduleReq.getDiscountRate()); // 设置优惠时段价格

				// 创建并设置普通时段
				List<TimeSlot> regularTimeSlots = scheduleReq.getRegularTimeSlots().stream().map(timeSlotReq -> {
					TimeSlot regularSlot = new TimeSlot();
					regularSlot.setStartTime(timeSlotReq.getStartTime());
					regularSlot.setEndTime(timeSlotReq.getEndTime());
					regularSlot.setIsDiscount(false); // 一般时段标记为非优惠时段
					regularSlot.setSchedule(schedule);
					return regularSlot;
				}).collect(Collectors.toList());

				// 创建并设置优惠时段
				List<TimeSlot> discountTimeSlots = scheduleReq.getDiscountTimeSlots().stream().map(timeSlotReq -> {
					TimeSlot discountSlot = new TimeSlot();
					discountSlot.setStartTime(timeSlotReq.getStartTime());
					discountSlot.setEndTime(timeSlotReq.getEndTime());
					discountSlot.setIsDiscount(true); // 优惠时段标记为优惠时段
					discountSlot.setSchedule(schedule);
					return discountSlot;
				}).collect(Collectors.toList());

				// 设置时间段列表
				schedule.setRegularTimeSlots(regularTimeSlots);
				schedule.setDiscountTimeSlots(discountTimeSlots);

				return schedule;
			}).collect(Collectors.toSet());

			// 保存所有定价计划
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

		// 處理 pricingSchedules，這裡可以加上一些額外邏輯來過濾或處理
		if (store.getPricingSchedules() != null) {
			builder.pricingSchedules(store.getPricingSchedules());
		} else {
			builder.pricingSchedules(Set.of()); // 如果為 null，可以設置為空集合
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

			// 更新供应商和池桌信息
			if (storeReq.getVendor() != null) {
				store.setVendor(storeReq.getVendor());
			}
			if (storeReq.getPoolTables() != null) {
				store.setPoolTables(storeReq.getPoolTables());
			}

			// 处理定价计划更新
			if (storeReq.getPricingSchedules() != null) {
				// 清空原有定价计划，避免 orphanRemoval 异常
				store.getPricingSchedules().clear();

				for (StorePricingScheduleReq scheduleReq : storeReq.getPricingSchedules()) {
					// 更新或创建新的定价计划
					StorePricingSchedule schedule = new StorePricingSchedule();
					schedule.setDayOfWeek(scheduleReq.getDayOfWeek());

					// 创建并设置普通时段
					List<TimeSlot> regularTimeSlots = new ArrayList<>();
					for (TimeSlotReq timeSlotReq : scheduleReq.getRegularTimeSlots()) {
						TimeSlot regularSlot = new TimeSlot();
						regularSlot.setStartTime(timeSlotReq.getStartTime());
						regularSlot.setEndTime(timeSlotReq.getEndTime());
						regularSlot.setIsDiscount(false); // 标记为普通时段
						regularSlot.setSchedule(schedule); // 设置关联关系
						regularTimeSlots.add(regularSlot);
					}

					// 创建并设置优惠时段
					List<TimeSlot> discountTimeSlots = new ArrayList<>();
					for (TimeSlotReq timeSlotReq : scheduleReq.getDiscountTimeSlots()) {
						TimeSlot discountSlot = new TimeSlot();
						discountSlot.setStartTime(timeSlotReq.getStartTime());
						discountSlot.setEndTime(timeSlotReq.getEndTime());
						discountSlot.setIsDiscount(true); // 标记为优惠时段
						discountSlot.setSchedule(schedule); // 设置关联关系
						discountTimeSlots.add(discountSlot);
					}

					// 设置时间段
					schedule.setRegularTimeSlots(regularTimeSlots);
					schedule.setDiscountTimeSlots(discountTimeSlots);

					schedule.setStore(store); // 关联 Store 实体
					store.getPricingSchedules().add(schedule); // 添加到 Store 的定价计划中
				}
			}

			// 更新修改时间和修改用户
			store.setUpdateTime(LocalDateTime.now());
			store.setUpdateUserId(id);

			// 保存并返回更新后的 store 实体
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
