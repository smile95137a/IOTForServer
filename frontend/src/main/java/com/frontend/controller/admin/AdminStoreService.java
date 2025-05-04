package com.frontend.controller.admin;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import com.frontend.entity.news.News;
import com.frontend.entity.role.Role;
import com.frontend.entity.store.StorePricingSchedule;
import com.frontend.entity.store.TimeSlot;
import com.frontend.entity.user.User;
import com.frontend.entity.vendor.Vendor;
import com.frontend.repo.StorePricingScheduleRepository;
import com.frontend.repo.TimeSlotRepository;
import com.frontend.repo.UserRepository;
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

	@Autowired
	private TimeSlotRepository timeSlotRepository;

	@Autowired
	private UserRepository userRepository;

	// Create a new store
	@Transactional
	public Store createStore(StoreReq storeReq, Long userId) {
		Store store = convertToEntity(storeReq);
		store.setBookTime(storeReq.getBookTime() == null ? 0 : storeReq.getBookTime());
		store.setCancelBookTime(storeReq.getCancelBookTime() == null ? 0 : storeReq.getCancelBookTime());
		store.setUid(RandomUtils.genRandom(24));
		store.setCreateTime(LocalDateTime.now());
		store.setCreateUserId(userId);
		store.setImgUrl(storeReq.getImgUrl() != null ? storeReq.getImgUrl() : "");
		store.setUser(storeReq.getUser());

		Store savedStore = storeRepository.save(store);

		// 為 7 天建立 schedule
		List<StorePricingSchedule> schedules = new ArrayList<>();
		for (DayOfWeek day : DayOfWeek.values()) {
			StorePricingSchedule schedule = new StorePricingSchedule();
			schedule.setDayOfWeek(day.name());
			schedule.setOpenTime(storeReq.getOpenTime());
			schedule.setCloseTime(storeReq.getCloseTime());
			schedule.setRegularRate(storeReq.getRegularRate());
			schedule.setDiscountRate(storeReq.getDiscountRate());
			schedule.setStore(savedStore);

			// 時段切分邏輯
			List<TimeSlot> timeSlots;
			try {
				timeSlots = splitTimeSlots(
						storeReq.getOpenTime(),
						storeReq.getCloseTime(),
						storeReq.getTimeSlots(),
						schedule
				);
			} catch (Exception e) {
				throw new RuntimeException("優惠時段錯誤：" + e.getMessage());
			}

			schedule.setTimeSlots(timeSlots);
			schedules.add(schedule);
		}

		storePricingScheduleRepository.saveAll(schedules);

		return savedStore;
	}

	private List<TimeSlot> splitTimeSlots(LocalTime openTime, LocalTime closeTime,
										  List<TimeSlotReq> timeSlotsReq, StorePricingSchedule schedule) throws Exception {
		List<TimeSlot> result = new ArrayList<>();

		// 将所有时段按开始时间排序
		List<LocalTime[]> allSlots = timeSlotsReq.stream()
				.map(slot -> new LocalTime[]{slot.getStartTime(), slot.getEndTime()})
				.sorted(Comparator.comparing(slot -> slot[0]))
				.toList();

		LocalTime current = openTime;
		LocalTime previousEndTime = openTime; // 用來跟前一個時段的結束時間進行比較
		// 遍历所有时段，处理时段分配
		for (LocalTime[] timeSlot : allSlots) {
			LocalTime slotStart = timeSlot[0];
			LocalTime slotEnd = timeSlot[1];

			// 檢查時段是否重疊
			if (slotStart.isBefore(previousEndTime)) {
				throw new Exception("優惠時段有重疊：" + previousEndTime + " - " + slotStart);
			}

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
				.imgUrl(store.getImgUrl())
				.address(store.getAddress())
				.vendor(store.getVendor())
				.lat(store.getLat())
				.lon(store.getLon())
				.deposit(store.getDeposit())
				.poolTables(store.getPoolTables()) // 如果 poolTables 不会导致循环引用，保持这个字段
				.hint(store.getHint())
				.contactPhone(store.getContactPhone())
				.bookTime(store.getBookTime())
				.cancelBookTime(store.getCancelBookTime())
				.user(store.getUser());

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
				.openTime(pricingSchedule.getOpenTime())
				.closeTime(pricingSchedule.getCloseTime())
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
	@Transactional
	public Store updateStore(String uid, StoreReq storeReq, Long id) throws Exception {
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
			store.setUser(storeReq.getUser());

			if (storeReq.getVendor() != null) {
				store.setVendor(storeReq.getVendor());
			}

			if (storeReq.getPoolTables() != null) {
				store.setPoolTables(storeReq.getPoolTables());
			}

			// 更新定价计划
			if (storeReq.getPricingSchedules() != null) {
				// 删除原有的定价计划及其时段，避免 orphanRemoval 问题
				store.getPricingSchedules().forEach(storePricingScheduleRepository::delete);
				store.getPricingSchedules().clear();

				List<StorePricingSchedule> schedules = new ArrayList<>();
				List<TimeSlot> timeSlots = new ArrayList<>();

				for (StorePricingScheduleReq scheduleReq : storeReq.getPricingSchedules()) {
					StorePricingSchedule schedule = new StorePricingSchedule();
					schedule.setDayOfWeek(scheduleReq.getDayOfWeek());
					schedule.setOpenTime(scheduleReq.getOpenTime());
					schedule.setCloseTime(scheduleReq.getCloseTime());
					schedule.setRegularRate(scheduleReq.getRegularRate());
					schedule.setDiscountRate(scheduleReq.getDiscountRate());
					schedule.setStore(store);

					// 时段切分逻辑
					List<TimeSlot> splitTimeSlots;
					try {
						splitTimeSlots = splitTimeSlots(scheduleReq.getOpenTime(), scheduleReq.getCloseTime(), scheduleReq.getTimeSlots(), schedule);
					} catch (Exception e) {
						throw new RuntimeException("时段切分错误：" + e.getMessage());
					}

					// 更新定价计划的时段
					schedule.setTimeSlots(splitTimeSlots);
					timeSlots.addAll(splitTimeSlots);
					schedules.add(schedule);
				}

				// 保存新的定价计划和时段
				storePricingScheduleRepository.saveAll(schedules);
				timeSlotRepository.saveAll(timeSlots);

				// 添加新的定价计划
				store.getPricingSchedules().addAll(schedules);
			}

			// 更新修改时间和修改用户
			store.setUpdateTime(LocalDateTime.now());
			store.setUpdateUserId(id);

			// 保存并返回更新后的 store 实体
			return storeRepository.save(store);
		}).orElseThrow(() -> new Exception("Store not found with uid: " + uid));
	}





	@Transactional
	public void deleteStore(String uid) {
		Store store = storeRepository.findByUid(uid)
				.orElseThrow(() -> new RuntimeException("找不到該 Store: " + uid));

		// 標記主體為已刪除
		store.setDeleted(true);

		// 標記 poolTables 為已刪除
		if (store.getPoolTables() != null) {
			store.getPoolTables().forEach(table -> table.setDeleted(true));
		}

		// 標記 pricingSchedules 為已刪除
		if (store.getPricingSchedules() != null) {
			store.getPricingSchedules().forEach(schedule -> schedule.setDeleted(true));
		}

		// 標記 routers 為已刪除
		if (store.getRouters() != null) {
			store.getRouters().forEach(router -> router.setDeleted(true));
		}

		// 儲存變更（透過級聯 cascade，一起更新關聯）
		storeRepository.save(store);
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

	public List<AdminStoreRes> getStoresByUserId(Long userId) {
		User user = userRepository.findById(userId).orElse(null);
		if (user == null) {
			return Collections.emptyList();
		}

		Set<Long> roleIds = user.getRoles().stream()
				.map(Role::getId)
				.collect(Collectors.toSet());

		List<Store> stores = new ArrayList<>();

		// 如果是管理員，顯示所有店家
		if (roleIds.contains(1L)) {
			return storeRepository.findAll().stream()
					.map(this::convertToAdminStoreRes)
					.collect(Collectors.toList());
		}

		if (roleIds.contains(2L)) {
			try {
				Vendor vendor = user.getVendor();
				if (vendor != null) {
					Long vendorId = vendor.getId();
					if (vendorId != null) {
						stores.addAll(storeRepository.findByVendorId(vendorId));
					} else {
						System.err.println("用戶 " + userId + " 的廠商ID為null");
					}
				} else {
					System.err.println("用戶 " + userId + " 沒有關聯的廠商");
				}
			} catch (Exception e) {
				System.err.println("獲取廠商時發生錯誤: " + e.getMessage());
				e.printStackTrace();
			}
		}

		// 如果是店家，添加自己的店
		if (roleIds.contains(5L)) {
			stores.addAll(storeRepository.findByUserId(userId));
		}

		// 去重
		return stores.stream()
				.distinct()
				.map(this::convertToAdminStoreRes)
				.collect(Collectors.toList());
	}

}
