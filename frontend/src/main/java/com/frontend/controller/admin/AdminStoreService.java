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
	@Transactional
	public Store createStore(StoreReq storeReq, Long userId) {
		// 创建并保存 Store 实体
		Store store = convertToEntity(storeReq);
		store.setUid(RandomUtils.genRandom(24)); // 生成唯一 UID
		store.setCreateTime(LocalDateTime.now());
		store.setCreateUserId(userId);
		store.setImgUrl(storeReq.getImgUrl() != null ? storeReq.getImgUrl() : ""); // 确保图片 URL
		Store savedStore = storeRepository.save(store);

		// 处理并保存 StorePricingSchedule 实体
		if (storeReq.getPricingSchedules() != null && !storeReq.getPricingSchedules().isEmpty()) {
			Set<StorePricingSchedule> schedules = storeReq.getPricingSchedules().stream().map(scheduleReq -> {
				StorePricingSchedule schedule = new StorePricingSchedule();
				schedule.setDiscountRate(storeReq.getDiscountRate());
				schedule.setRegularRate(storeReq.getRegularRate());
				schedule.setDayOfWeek(scheduleReq.getDayOfWeek());
				schedule.setStore(savedStore); // 关联 Store

				// 处理时段
				List<TimeSlot> regularTimeSlots = splitTimeSlots(scheduleReq.getRegularTimeSlots(), scheduleReq.getDiscountTimeSlots(), false, schedule);
				List<TimeSlot> discountTimeSlots = splitTimeSlots(scheduleReq.getDiscountTimeSlots(), scheduleReq.getRegularTimeSlots(), true, schedule);

				schedule.setRegularTimeSlots(regularTimeSlots);
				schedule.setDiscountTimeSlots(discountTimeSlots);

				return schedule;
			}).collect(Collectors.toSet());

			// 保存 StorePricingSchedule 实体
			storePricingScheduleRepository.saveAll(schedules);
		}

		return savedStore;
	}

	// 修改方法签名，增加 StorePricingSchedule 参数
	private List<TimeSlot> splitTimeSlots(List<TimeSlotReq> timeSlots, List<TimeSlotReq> overlappingTimeSlots, boolean isDiscount, StorePricingSchedule schedule) {
		List<TimeSlot> result = new ArrayList<>();

		if (timeSlots == null || timeSlots.isEmpty()) {
			return result;
		}

		// 如果没有重叠时间段，直接创建 TimeSlot
		if (overlappingTimeSlots == null || overlappingTimeSlots.isEmpty()) {
			return timeSlots.stream()
					.map(slot -> {
						TimeSlot timeSlot = new TimeSlot(slot.getStartTime(), slot.getEndTime(), isDiscount);
						// 始终设置 regularSchedule
						timeSlot.setRegularSchedule(schedule);

						// 无论是否为折扣，都设置 discountSchedule
						// 如果不是折扣时间，使用当前 schedule 作为 discountSchedule
						timeSlot.setDiscountSchedule(isDiscount ? schedule : schedule);

						return timeSlot;
					})
					.collect(Collectors.toList());
		}

		for (TimeSlotReq timeSlotReq : timeSlots) {
			LocalTime start = timeSlotReq.getStartTime();
			LocalTime end = timeSlotReq.getEndTime();
			boolean hasOverlap = false;

			for (TimeSlotReq overlappingSlot : overlappingTimeSlots) {
				LocalTime overlappingStart = overlappingSlot.getStartTime();
				LocalTime overlappingEnd = overlappingSlot.getEndTime();

				if (start.isBefore(overlappingEnd) && end.isAfter(overlappingStart)) {
					hasOverlap = true;

					// 非重叠部分（前段）
					if (start.isBefore(overlappingStart)) {
						TimeSlot timeSlot = new TimeSlot(start, overlappingStart, isDiscount);
						timeSlot.setRegularSchedule(schedule);
						timeSlot.setDiscountSchedule(isDiscount ? schedule : schedule);
						result.add(timeSlot);
					}

					// 重叠部分，始终作为折扣时段
					TimeSlot discountSlot = new TimeSlot(overlappingStart, overlappingEnd, true);
					discountSlot.setRegularSchedule(schedule);
					discountSlot.setDiscountSchedule(schedule);
					result.add(discountSlot);

					// 非重叠部分（后段）
					if (end.isAfter(overlappingEnd)) {
						TimeSlot timeSlot = new TimeSlot(overlappingEnd, end, isDiscount);
						timeSlot.setRegularSchedule(schedule);
						timeSlot.setDiscountSchedule(isDiscount ? schedule : schedule);
						result.add(timeSlot);
					}

					start = end;
				}
			}

			// 无重叠时间段
			if (!hasOverlap) {
				TimeSlot timeSlot = new TimeSlot(timeSlotReq.getStartTime(), timeSlotReq.getEndTime(), isDiscount);
				timeSlot.setRegularSchedule(schedule);
				timeSlot.setDiscountSchedule(isDiscount ? schedule : schedule);
				result.add(timeSlot);
			}
		}

		return result;
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
			// 更新店铺基本信息
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

			// 更新定价计划
			if (storeReq.getPricingSchedules() != null) {
				// 清空原有定价计划，避免 orphanRemoval 异常
				store.getPricingSchedules().clear();

				for (StorePricingScheduleReq scheduleReq : storeReq.getPricingSchedules()) {
					// 创建或更新定价计划
					StorePricingSchedule schedule = new StorePricingSchedule();
					schedule.setDayOfWeek(scheduleReq.getDayOfWeek());

					// 创建并设置普通时段
					// 创建普通时段
					List<TimeSlot> regularTimeSlots = new ArrayList<>();
					for (TimeSlotReq timeSlotReq : scheduleReq.getRegularTimeSlots()) {
						TimeSlot regularSlot = new TimeSlot();
						regularSlot.setStartTime(timeSlotReq.getStartTime());
						regularSlot.setEndTime(timeSlotReq.getEndTime());
						regularSlot.setIsDiscount(false); // 标记为普通时段
						regularSlot.setRegularSchedule(schedule); // 設置關聯為當前的 schedule
						regularTimeSlots.add(regularSlot);
					}

// 创建优惠时段
					List<TimeSlot> discountTimeSlots = new ArrayList<>();
					for (TimeSlotReq timeSlotReq : scheduleReq.getDiscountTimeSlots()) {
						TimeSlot discountSlot = new TimeSlot();
						discountSlot.setStartTime(timeSlotReq.getStartTime());
						discountSlot.setEndTime(timeSlotReq.getEndTime());
						discountSlot.setIsDiscount(true); // 标记为优惠时段
						discountSlot.setDiscountSchedule(schedule); // 設置關聯為當前的 schedule
						discountTimeSlots.add(discountSlot);
					}

// 设定 schedule 的时间段
					schedule.setRegularTimeSlots(regularTimeSlots);
					schedule.setDiscountTimeSlots(discountTimeSlots);

					// 关联 Store 实体
					schedule.setStore(store);
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
