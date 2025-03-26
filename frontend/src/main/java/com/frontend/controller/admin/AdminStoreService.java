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
import com.frontend.res.store.StorePricingScheduleRes;
import com.frontend.res.store.StoreRes;
import com.frontend.res.store.TimeSlotRes;
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
		// 建立 Store 物件
		Store store = convertToEntity(storeReq);
		store.setBookTime(storeReq.getBookTime() == null ? 0 : storeReq.getBookTime());
		store.setCancelBookTime(storeReq.getCancelBookTime() == null ? 0 : storeReq.getCancelBookTime());
		store.setUid(RandomUtils.genRandom(24));
		store.setCreateTime(LocalDateTime.now());
		store.setCreateUserId(userId);
		store.setImgUrl(storeReq.getImgUrl() != null ? storeReq.getImgUrl() : "");

// ✅ 先儲存 Store
		Store savedStore = storeRepository.save(store);

// ✅ 再來建立 Schedule 並設定回 Store
		if (storeReq.getPricingSchedules() != null && !storeReq.getPricingSchedules().isEmpty()) {
			Set<StorePricingSchedule> schedules = storeReq.getPricingSchedules().stream().map(scheduleReq -> {
				StorePricingSchedule schedule = new StorePricingSchedule();
				schedule.setDiscountRate(scheduleReq.getDiscountRate());
				schedule.setRegularRate(scheduleReq.getRegularRate());
				schedule.setDayOfWeek(scheduleReq.getDayOfWeek());
				schedule.setStore(savedStore); // 關聯 Store

				List<TimeSlot> regularTimeSlots = splitTimeSlots(scheduleReq.getRegularTimeSlots(), scheduleReq.getDiscountTimeSlots(), false, schedule);
				List<TimeSlot> discountTimeSlots = splitTimeSlots(scheduleReq.getDiscountTimeSlots(), scheduleReq.getRegularTimeSlots(), true, schedule);

				schedule.setRegularTimeSlots(regularTimeSlots);
				schedule.setDiscountTimeSlots(discountTimeSlots);

				return schedule;
			}).collect(Collectors.toSet());

			// ✅ 記得把 schedules 設回 Store（建立雙向關聯）
			savedStore.setPricingSchedules(schedules);

			// ✅ 最後一起 saveAll schedules
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

		for (TimeSlotReq timeSlotReq : timeSlots) {
			// 处理空值，跳过无效时间段
			if (timeSlotReq.getStartTime() == null || timeSlotReq.getEndTime() == null) {
				continue;
			}

			LocalTime start = timeSlotReq.getStartTime();
			LocalTime end = timeSlotReq.getEndTime();
			boolean hasOverlap = false;

			for (TimeSlotReq overlappingSlot : overlappingTimeSlots) {
				if (overlappingSlot.getStartTime() == null || overlappingSlot.getEndTime() == null) {
					continue;
				}

				LocalTime overlappingStart = overlappingSlot.getStartTime();
				LocalTime overlappingEnd = overlappingSlot.getEndTime();

				if (start.isBefore(overlappingEnd) && end.isAfter(overlappingStart)) {
					hasOverlap = true;

					// 处理前段非重叠部分
					if (start.isBefore(overlappingStart)) {
						TimeSlot timeSlot = new TimeSlot(start, overlappingStart, isDiscount);
						timeSlot.setRegularSchedule(schedule);
						timeSlot.setDiscountSchedule(schedule);
						result.add(timeSlot);
					}

					// 处理重叠部分
					TimeSlot discountSlot = new TimeSlot(overlappingStart, overlappingEnd, true);
					discountSlot.setRegularSchedule(schedule);
					discountSlot.setDiscountSchedule(schedule);
					result.add(discountSlot);

					// 处理后段非重叠部分
					if (end.isAfter(overlappingEnd)) {
						TimeSlot timeSlot = new TimeSlot(overlappingEnd, end, isDiscount);
						timeSlot.setRegularSchedule(schedule);
						timeSlot.setDiscountSchedule(schedule);
						result.add(timeSlot);
					}
				}
			}

			// 如果没有重叠，直接添加时间段
			if (!hasOverlap) {
				TimeSlot timeSlot = new TimeSlot(start, end, isDiscount);
				timeSlot.setRegularSchedule(schedule);
				timeSlot.setDiscountSchedule(schedule);
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
				.poolTables(store.getPoolTables()) // 如果 poolTables 不会导致循环引用，保持这个字段
				.hint(store.getHint())
				.contactPhone(store.getContactPhone())
				.bookTime(store.getBookTime())
				.cancelBookTime(store.getCancelBookTime());

		// 将 pricingSchedules 转换为 StorePricingScheduleRes
		if (store.getPricingSchedules() != null) {
			builder.pricingSchedules(store.getPricingSchedules().stream()
					.map(pricingSchedule -> convertToStorePricingScheduleRes(pricingSchedule)) // 转换为 StorePricingScheduleRes
					.collect(Collectors.toSet()));
		} else {
			builder.pricingSchedules(Set.of()); // 如果为 null，设为空集合
		}

		return builder.build();
	}

	// 将 StorePricingSchedule 转换为 StorePricingScheduleRes 的方法
	private StorePricingScheduleRes convertToStorePricingScheduleRes(StorePricingSchedule pricingSchedule) {
		return StorePricingScheduleRes.builder()
				.dayOfWeek(pricingSchedule.getDayOfWeek())
				.regularTimeSlots(pricingSchedule.getRegularTimeSlots().stream()
						.map(this::convertToTimeSlotRes) // 转换 regularTimeSlots
						.collect(Collectors.toList()))
				.discountTimeSlots(pricingSchedule.getDiscountTimeSlots().stream()
						.map(this::convertToTimeSlotRes) // 转换 discountTimeSlots
						.collect(Collectors.toList()))
				.regularRate(pricingSchedule.getRegularRate())
				.discountRate(pricingSchedule.getDiscountRate())
				.build();
	}

	// 将 TimeSlot 转换为 TimeSlotRes 的方法
	private TimeSlotRes convertToTimeSlotRes(TimeSlot timeSlot) {
		return new TimeSlotRes(timeSlot.getStartTime(), timeSlot.getEndTime(), timeSlot.getIsDiscount());
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
			store.setBookTime(storeReq.getBookTime() == null ? 0 : storeReq.getBookTime());
			store.setCancelBookTime(storeReq.getCancelBookTime() == null ? 0 : storeReq.getCancelBookTime());
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
