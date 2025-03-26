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
		Store store = convertToEntity(storeReq);
		store.setBookTime(Optional.ofNullable(storeReq.getBookTime()).orElse(0));
		store.setCancelBookTime(Optional.ofNullable(storeReq.getCancelBookTime()).orElse(0));
		store.setUid(RandomUtils.genRandom(24));
		store.setCreateTime(LocalDateTime.now());
		store.setCreateUserId(userId);
		store.setImgUrl(Optional.ofNullable(storeReq.getImgUrl()).orElse(""));

		Store savedStore = storeRepository.save(store);

		if (storeReq.getPricingSchedules() != null && !storeReq.getPricingSchedules().isEmpty()) {
			Set<StorePricingSchedule> schedules = storeReq.getPricingSchedules().stream().map(scheduleReq -> {
				StorePricingSchedule schedule = new StorePricingSchedule();
				schedule.setDiscountRate(scheduleReq.getDiscountRate());
				schedule.setRegularRate(scheduleReq.getRegularRate());
				schedule.setDayOfWeek(scheduleReq.getDayOfWeek());
				schedule.setStore(savedStore);

				// 將一般和優惠時段整合處理
				List<TimeSlot> allSlots = new ArrayList<>();
				if (scheduleReq.getRegularTimeSlots() != null) {
					for (TimeSlotReq ts : scheduleReq.getRegularTimeSlots()) {
						TimeSlot slot = new TimeSlot(ts.getStartTime(), ts.getEndTime(), false);
						slot.setSchedule(schedule);
						allSlots.add(slot);
					}
				}
				if (scheduleReq.getDiscountTimeSlots() != null) {
					for (TimeSlotReq ts : scheduleReq.getDiscountTimeSlots()) {
						TimeSlot slot = new TimeSlot(ts.getStartTime(), ts.getEndTime(), true);
						slot.setSchedule(schedule);
						allSlots.add(slot);
					}
				}
				schedule.setTimeSlots(new HashSet<>(allSlots));

				return schedule;
			}).collect(Collectors.toSet());

			savedStore.setPricingSchedules(schedules);
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
						timeSlot.setSchedule(schedule);
						result.add(timeSlot);
					}

					// 处理重叠部分
					TimeSlot discountSlot = new TimeSlot(overlappingStart, overlappingEnd, true);
					discountSlot.setSchedule(schedule);
					result.add(discountSlot);

					// 处理后段非重叠部分
					if (end.isAfter(overlappingEnd)) {
						TimeSlot timeSlot = new TimeSlot(overlappingEnd, end, isDiscount);
						timeSlot.setSchedule(schedule);
						result.add(timeSlot);
					}
				}
			}

			// 如果没有重叠，直接添加时间段
			if (!hasOverlap) {
				TimeSlot timeSlot = new TimeSlot(start, end, isDiscount);
				timeSlot.setSchedule(schedule);
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
				.regularTimeSlots(
						pricingSchedule.getTimeSlots().stream()
								.filter(ts -> !ts.getIsDiscount())
								.map(this::convertToTimeSlotRes)
								.collect(Collectors.toList())
				)
				.discountTimeSlots(
						pricingSchedule.getTimeSlots().stream()
								.filter(TimeSlot::getIsDiscount)
								.map(this::convertToTimeSlotRes)
								.collect(Collectors.toList())
				)
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
			// 更新店鋪基本資料
			store.setName(storeReq.getName());
			store.setAddress(storeReq.getAddress());
			store.setLat(storeReq.getLat());
			store.setLon(storeReq.getLon());
			store.setDeposit(storeReq.getDeposit());
			store.setHint(storeReq.getHint());
			store.setContactPhone(storeReq.getContactPhone());
			store.setBookTime(storeReq.getBookTime() == null ? 0 : storeReq.getBookTime());
			store.setCancelBookTime(storeReq.getCancelBookTime() == null ? 0 : storeReq.getCancelBookTime());

			if (storeReq.getVendor() != null) {
				store.setVendor(storeReq.getVendor());
			}
			if (storeReq.getPoolTables() != null) {
				store.setPoolTables(storeReq.getPoolTables());
			}

			// 更新 pricingSchedules
			if (storeReq.getPricingSchedules() != null) {
				// 清空原本的 schedules（為了讓 JPA orphanRemoval 生效）
				store.getPricingSchedules().clear();

				for (StorePricingScheduleReq scheduleReq : storeReq.getPricingSchedules()) {
					StorePricingSchedule schedule = new StorePricingSchedule();
					schedule.setDayOfWeek(scheduleReq.getDayOfWeek());
					schedule.setRegularRate(scheduleReq.getRegularRate());
					schedule.setDiscountRate(scheduleReq.getDiscountRate());
					schedule.setStore(store); // 關聯 Store

					Set<TimeSlot> timeSlots = new HashSet<>();

					for (TimeSlotReq timeSlotReq : scheduleReq.getRegularTimeSlots()) {
						TimeSlot slot = new TimeSlot();
						slot.setStartTime(timeSlotReq.getStartTime());
						slot.setEndTime(timeSlotReq.getEndTime());
						slot.setIsDiscount(false); // 一般時段
						slot.setSchedule(schedule); // 單一欄位 schedule
						timeSlots.add(slot);
					}

					for (TimeSlotReq timeSlotReq : scheduleReq.getDiscountTimeSlots()) {
						TimeSlot slot = new TimeSlot();
						slot.setStartTime(timeSlotReq.getStartTime());
						slot.setEndTime(timeSlotReq.getEndTime());
						slot.setIsDiscount(true); // 優惠時段
						slot.setSchedule(schedule); // 單一欄位 schedule
						timeSlots.add(slot);
					}

					schedule.setTimeSlots(timeSlots);
					store.getPricingSchedules().add(schedule);
				}
			}

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
