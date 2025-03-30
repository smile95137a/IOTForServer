package com.frontend.controller.admin;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
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
		// 创建并保存 Store 实体
		Store store = convertToEntity(storeReq);
		store.setBookTime(storeReq.getBookTime() == null ? 0 : storeReq.getBookTime());
		store.setCancelBookTime(storeReq.getCancelBookTime() == null ? 0 : storeReq.getCancelBookTime());
		store.setUid(RandomUtils.genRandom(24));
		store.setCreateTime(LocalDateTime.now());
		store.setCreateUserId(userId);
		store.setImgUrl(storeReq.getImgUrl() != null ? storeReq.getImgUrl() : "");
		Store savedStore = storeRepository.save(store);

		// 保存 StorePricingSchedule 与 TimeSlot
		if (storeReq.getPricingSchedules() != null && !storeReq.getPricingSchedules().isEmpty()) {
			Set<StorePricingSchedule> schedules = storeReq.getPricingSchedules().stream()
					.map(scheduleReq -> {
						StorePricingSchedule schedule = new StorePricingSchedule();
						schedule.setDayOfWeek(scheduleReq.getDayOfWeek());
						schedule.setOpenTime(scheduleReq.getOpenTime());
						schedule.setCloseTime(scheduleReq.getCloseTime());
						schedule.setRegularRate(storeReq.getRegularRate());
						schedule.setDiscountRate(storeReq.getDiscountRate());
						schedule.setStore(savedStore);

						// 🔥 自动划分时段
						List<TimeSlot> timeSlots = splitTimeSlots(
								scheduleReq.getOpenTime(),
								scheduleReq.getCloseTime(),
								scheduleReq.getTimeSlots(),
								schedule
						);
						schedule.setTimeSlots(timeSlots);

						return schedule;
					}).collect(Collectors.toSet());

			storePricingScheduleRepository.saveAll(schedules);
		}

		return savedStore;
	}

	private List<TimeSlot> splitTimeSlots(LocalTime openTime, LocalTime closeTime,
										  List<TimeSlotReq> timeSlotsReq, StorePricingSchedule schedule) {
		List<TimeSlot> result = new ArrayList<>();

		// 将所有时段按开始时间排序
		List<LocalTime[]> allSlots = timeSlotsReq.stream()
				.map(slot -> new LocalTime[]{slot.getStartTime(), slot.getEndTime()})
				.sorted(Comparator.comparing(slot -> slot[0]))
				.toList();

		LocalTime current = openTime;

		// 遍历所有时段，处理时段分配
		for (LocalTime[] timeSlot : allSlots) {
			LocalTime slotStart = timeSlot[0];
			LocalTime slotEnd = timeSlot[1];

			// 非优惠时段（在优惠开始前的时段）
			if (current.isBefore(slotStart)) {
				result.add(createTimeSlot(current, slotStart, false, schedule));
			}

			// 优惠时段
			result.add(createTimeSlot(slotStart, slotEnd, true, schedule));

			current = slotEnd;
		}

		// 最后剩余的一般时段
		if (current.isBefore(closeTime)) {
			result.add(createTimeSlot(current, closeTime, false, schedule));
		}

		return result;
	}

	private TimeSlot createTimeSlot(LocalTime startTime, LocalTime endTime, boolean isDiscount, StorePricingSchedule schedule) {
		TimeSlot slot = new TimeSlot();
		slot.setStartTime(startTime);
		slot.setEndTime(endTime);
		slot.setIsDiscount(isDiscount);
		slot.setSchedule(schedule);
		return slot;
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
		// 将统一的时间段列表转换为普通时段和优惠时段
		List<TimeSlotRes> regularTimeSlots = pricingSchedule.getTimeSlots().stream()
				.filter(timeSlot -> !timeSlot.getIsDiscount()) // 筛选出普通时段
				.map(this::convertToTimeSlotRes) // 转换为 TimeSlotRes
				.collect(Collectors.toList());

		List<TimeSlotRes> discountTimeSlots = pricingSchedule.getTimeSlots().stream()
				.filter(timeSlot -> timeSlot.getIsDiscount()) // 筛选出优惠时段
				.map(this::convertToTimeSlotRes) // 转换为 TimeSlotRes
				.collect(Collectors.toList());

		return StorePricingScheduleRes.builder()
				.dayOfWeek(pricingSchedule.getDayOfWeek()) // 设置星期几
				.regularTimeSlots(regularTimeSlots) // 设置普通时段
				.discountTimeSlots(discountTimeSlots) // 设置优惠时段
				.regularRate(pricingSchedule.getRegularRate()) // 设置普通时段价格
				.discountRate(pricingSchedule.getDiscountRate()) // 设置优惠时段价格
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
					// 创建定价计划
					StorePricingSchedule schedule = new StorePricingSchedule();
					schedule.setDayOfWeek(scheduleReq.getDayOfWeek());
					schedule.setOpenTime(scheduleReq.getOpenTime());
					schedule.setCloseTime(scheduleReq.getCloseTime());
					schedule.setRegularRate(scheduleReq.getRegularRate());
					schedule.setDiscountRate(scheduleReq.getDiscountRate());
					// 创建统一的时间段列表
					List<TimeSlot> timeSlots = new ArrayList<>();
					for (TimeSlotReq timeSlotReq : scheduleReq.getTimeSlots()) {
						TimeSlot timeSlot = new TimeSlot();
						timeSlot.setStartTime(timeSlotReq.getStartTime());
						timeSlot.setEndTime(timeSlotReq.getEndTime());
						timeSlot.setIsDiscount(timeSlotReq.getIsDiscount());
						timeSlot.setSchedule(schedule);
						timeSlots.add(timeSlot);
					}

					// 设定 schedule 的时间段
					schedule.setTimeSlots(timeSlots);

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
