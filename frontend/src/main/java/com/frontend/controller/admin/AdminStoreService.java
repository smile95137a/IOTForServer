package com.frontend.controller.admin;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import com.frontend.entity.news.News;
import com.frontend.entity.role.Role;
import com.frontend.entity.store.*;
import com.frontend.entity.user.User;
import com.frontend.entity.vendor.Vendor;
import com.frontend.repo.*;
import com.frontend.req.store.*;
import com.frontend.res.store.*;
import com.frontend.res.vendor.VendorDto;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

	@Autowired
	private SpecialDateRepository specialDateRepository;

	@Autowired
	private SpecialTimeSlotRepository specialTimeSlotRepository;
    @Autowired
    private VendorRepository vendorRepository;


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

		if(storeReq.getUser() == null) {
			Vendor vendor = vendorRepository.findById(storeReq.getVendor().getId()).get();
			User user = userRepository.findById(vendor.getUserId()).get();
			store.setUser(user);
		} else {
			store.setUser(storeReq.getUser());
		}

		Store savedStore = storeRepository.save(store);

		// 儲存特殊日期與時段
		saveSpecialDates(storeReq, savedStore);

		// 建立週間排程
		List<StorePricingSchedule> schedules = createWeeklySchedules(storeReq, savedStore);
		storePricingScheduleRepository.saveAll(schedules);

		return savedStore;
	}

	// 輔助方法：儲存特殊日期
	private void saveSpecialDates(StoreReq storeReq, Store savedStore) {
		List<SpecialDate> specialDates = new ArrayList<>();
		if (storeReq.getSpecialDates() != null) {
			for (SpecialDateReq dateReq : storeReq.getSpecialDates()) {
				SpecialDate specialDate = new SpecialDate();
				specialDate.setDate(dateReq.getDate());
				specialDate.setOpenTime(dateReq.getOpenTime());
				specialDate.setCloseTime(dateReq.getCloseTime());
				specialDate.setRegularRate(dateReq.getRegularRate());
				specialDate.setStore(savedStore);

				List<SpecialTimeSlot> slots = new ArrayList<>();
				for (SpecialTimeSlotReq slotReq : dateReq.getTimeSlots()) {
					SpecialTimeSlot slot = new SpecialTimeSlot();
					slot.setStartTime(slotReq.getStartTime());
					slot.setEndTime(slotReq.getEndTime());
					slot.setIsDiscount(slotReq.getIsDiscount());
					slot.setPrice(slotReq.getPrice());
					slot.setSpecialDate(specialDate);
					slots.add(slot);
				}
				specialDate.setTimeSlots(slots);
				specialDates.add(specialDate);
			}
			specialDateRepository.saveAll(specialDates);
		}
	}

	// 輔助方法：建立週間排程
	private List<StorePricingSchedule> createWeeklySchedules(StoreReq storeReq, Store savedStore) {
		List<StorePricingSchedule> schedules = new ArrayList<>();

		// 檢查是否啟用週末獨立設定
		boolean hasWeekendSetting = storeReq.getWeekendSchedule() != null &&
				storeReq.getWeekendSchedule().getEnableWeekendSetting();

		for (DayOfWeek day : DayOfWeek.values()) {
			StorePricingSchedule schedule = new StorePricingSchedule();
			schedule.setDayOfWeek(day.name());
			schedule.setStore(savedStore);

			// 判斷是平日還是週末
			boolean isWeekend = (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY);

			if (isWeekend && hasWeekendSetting) {
				// 週末使用獨立設定
				setWeekendSchedule(schedule, storeReq.getWeekendSchedule(), day);
			} else {
				// 平日使用預設設定
				setWeekdaySchedule(schedule, storeReq, day);
			}

			schedules.add(schedule);
		}

		return schedules;
	}

	// 設定平日排程
	private void setWeekdaySchedule(StorePricingSchedule schedule, StoreReq storeReq, DayOfWeek day) {
		schedule.setOpenTime(storeReq.getOpenTime());
		schedule.setCloseTime(storeReq.getCloseTime());
		schedule.setRegularRate(storeReq.getRegularRate());
		schedule.setDiscountRate(storeReq.getDiscountRate());

		// 只有 MONDAY 包含時段設定，其他平日使用相同設定但不重複儲存時段
		if (day == DayOfWeek.MONDAY) {
			try {
				List<TimeSlot> timeSlots = splitTimeSlots(
						storeReq.getOpenTime(),
						storeReq.getCloseTime(),
						storeReq.getTimeSlots(),
						schedule
				);
				schedule.setTimeSlots(timeSlots);
			} catch (Exception e) {
				throw new RuntimeException("平日優惠時段錯誤：" + e.getMessage());
			}
		} else {
			// 其他平日設置空的時段列表
			schedule.setTimeSlots(new ArrayList<>());
		}
	}

	// 設定週末排程
	private void setWeekendSchedule(StorePricingSchedule schedule, WeekendScheduleReq weekendReq, DayOfWeek day) {
		schedule.setOpenTime(weekendReq.getOpenTime());
		schedule.setCloseTime(weekendReq.getCloseTime());
		schedule.setRegularRate(weekendReq.getRegularRate());
		schedule.setDiscountRate(weekendReq.getDiscountRate());

		try {
			List<TimeSlot> timeSlots = splitTimeSlots(
					weekendReq.getOpenTime(),
					weekendReq.getCloseTime(),
					weekendReq.getTimeSlots(),
					schedule
			);
			schedule.setTimeSlots(timeSlots);
		} catch (Exception e) {
			throw new RuntimeException("週末優惠時段錯誤（" + day.name() + "）：" + e.getMessage());
		}
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
				.vendor(new VendorDto(
						store.getVendor().getId()
				))
				.lat(store.getLat())
				.lon(store.getLon())
				.deposit(store.getDeposit())
				.poolTables(store.getPoolTables())
				.hint(store.getHint())
				.contactPhone(store.getContactPhone())
				.bookTime(store.getBookTime())
				.cancelBookTime(store.getCancelBookTime())
				.user(store.getUser());

		// 定義 pricingSchedules 轉換
		if (store.getPricingSchedules() != null) {
			Set<StorePricingScheduleRes> pricingScheduleResSet = store.getPricingSchedules().stream()
					.map(this::convertToStorePricingScheduleRes)
					.collect(Collectors.toSet());

			builder.pricingSchedules(pricingScheduleResSet);

			// 取得平日設定（週一代表平日）
			Optional<StorePricingSchedule> weekdaySchedule = store.getPricingSchedules().stream()
					.filter(schedule -> "MONDAY".equals(schedule.getDayOfWeek()))
					.findFirst();

			// 取得週末設定
			Optional<StorePricingSchedule> saturdaySchedule = store.getPricingSchedules().stream()
					.filter(schedule -> "SATURDAY".equals(schedule.getDayOfWeek()))
					.findFirst();

			Optional<StorePricingSchedule> sundaySchedule = store.getPricingSchedules().stream()
					.filter(schedule -> "SUNDAY".equals(schedule.getDayOfWeek()))
					.findFirst();

			// 設定平日基本資料
			if (weekdaySchedule.isPresent()) {
				StorePricingSchedule weekday = weekdaySchedule.get();
				builder.openTime(weekday.getOpenTime());
				builder.closeTime(weekday.getCloseTime());
				builder.regularRate(weekday.getRegularRate());
				builder.discountRate(weekday.getDiscountRate());

				// 平日優惠時段（只取週一的時段，因為其他平日共用）
				List<TimeSlotRes> weekdayTimeSlots = weekday.getTimeSlots() != null ?
						weekday.getTimeSlots().stream()
								.filter(TimeSlot::getIsDiscount)
								.map(this::convertToTimeSlotRes)
								.collect(Collectors.toList()) :
						List.of();

				builder.timeSlots(weekdayTimeSlots);
			}

			// 檢查是否有週末獨立設定
			if (weekdaySchedule.isPresent() && saturdaySchedule.isPresent()) {
				StorePricingSchedule weekday = weekdaySchedule.get();
				StorePricingSchedule saturday = saturdaySchedule.get();

				// 判斷週末設定是否與平日不同
				boolean hasWeekendSetting = !Objects.equals(weekday.getOpenTime(), saturday.getOpenTime()) ||
						!Objects.equals(weekday.getCloseTime(), saturday.getCloseTime()) ||
						!Objects.equals(weekday.getRegularRate(), saturday.getRegularRate()) ||
						!Objects.equals(weekday.getDiscountRate(), saturday.getDiscountRate()) ||
						(saturday.getTimeSlots() != null && !saturday.getTimeSlots().isEmpty());

				if (hasWeekendSetting) {
					// 週末優惠時段
					List<TimeSlotRes> weekendTimeSlots = saturday.getTimeSlots() != null ?
							saturday.getTimeSlots().stream()
									.filter(TimeSlot::getIsDiscount)
									.map(this::convertToTimeSlotRes)
									.collect(Collectors.toList()) :
							List.of();

					// 構建週末設定
					WeekendScheduleRes weekendSchedule = WeekendScheduleRes.builder()
							.enableWeekendSetting(true)
							.openTime(saturday.getOpenTime())
							.closeTime(saturday.getCloseTime())
							.regularRate(saturday.getRegularRate())
							.discountRate(saturday.getDiscountRate())
							.timeSlots(weekendTimeSlots)
							.build();

					builder.weekendSchedule(weekendSchedule);

					// 合併所有優惠時段（平日 + 週末）
					List<TimeSlotRes> allTimeSlots = new ArrayList<>();
					if (weekdaySchedule.get().getTimeSlots() != null) {
						List<TimeSlotRes> weekdaySlots = weekdaySchedule.get().getTimeSlots().stream()
								.filter(TimeSlot::getIsDiscount)
								.map(this::convertToTimeSlotRes)
								.collect(Collectors.toList());
						allTimeSlots.addAll(weekdaySlots);
					}
					allTimeSlots.addAll(weekendTimeSlots);

					builder.timeSlots(allTimeSlots);
				}
			}

			// 如果沒有平日設定，使用第一個可用的設定作為預設
			if (weekdaySchedule.isEmpty()) {
				store.getPricingSchedules().stream().findFirst().ifPresent(first -> {
					builder.openTime(first.getOpenTime());
					builder.closeTime(first.getCloseTime());
					builder.regularRate(first.getRegularRate());
					builder.discountRate(first.getDiscountRate());
				});

				// 取得所有優惠時段
				List<TimeSlotRes> allTimeSlots = store.getPricingSchedules().stream()
						.filter(schedule -> schedule.getTimeSlots() != null && !schedule.getTimeSlots().isEmpty())
						.flatMap(schedule -> schedule.getTimeSlots().stream())
						.filter(TimeSlot::getIsDiscount)
						.map(this::convertToTimeSlotRes)
						.collect(Collectors.toList());

				builder.timeSlots(allTimeSlots);
			}
		} else {
			builder.pricingSchedules(Set.of());
			builder.timeSlots(List.of());
		}

		// 加入 specialDates 的處理邏輯
		if (store.getSpecialDates() != null) {
			List<SpecialDateRes> specialDateResList = store.getSpecialDates().stream()
					.map(specialDate -> SpecialDateRes.builder()
							.id(specialDate.getId())
							.date(specialDate.getDate().toString())
							.openTime(specialDate.getOpenTime())
							.closeTime(specialDate.getCloseTime())
							.regularRate(specialDate.getRegularRate())
							.timeSlots(specialDate.getTimeSlots() != null
									? specialDate.getTimeSlots().stream()
									.map(slot -> SpecialTimeSlotRes.builder()
											.id(slot.getId())
											.startTime(slot.getStartTime())
											.endTime(slot.getEndTime())
											.isDiscount(slot.getIsDiscount())
											.price(slot.getPrice())
											.build())
									.collect(Collectors.toList())
									: List.of())
							.build())
					.collect(Collectors.toList());

			builder.specialDates(specialDateResList);
		} else {
			builder.specialDates(List.of());
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
		if(timeSlot.getIsDiscount()) {
			return new TimeSlotRes(timeSlot.getStartTime(), timeSlot.getEndTime(), timeSlot.getIsDiscount() , timeSlot.getSchedule().getRegularRate());
		}
		return null;
	}





	// Update a store
	@Transactional
	public Store updateStore(String uid, StoreReq storeReq, Long id) throws Exception {
		return storeRepository.findByUid(uid).map(store -> {
			// 更新店鋪基本信息
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

			if (storeReq.getImgUrl() != null && !storeReq.getImgUrl().isEmpty()) {
				store.setImgUrl(storeReq.getImgUrl());
			}

			if (storeReq.getVendor() != null) {
				store.setVendor(storeReq.getVendor());
			}

			if (storeReq.getPoolTables() != null) {
				store.setPoolTables(storeReq.getPoolTables());
			}

			// 刪除原有的特殊日期及其時段
			if (store.getSpecialDates() != null) {
				store.getSpecialDates().forEach(specialDate -> {
					specialDate.getTimeSlots().forEach(timeSlot -> specialTimeSlotRepository.delete(timeSlot));
					specialDateRepository.delete(specialDate);
				});
				store.getSpecialDates().clear();
			}

			// 儲存新的特殊日期與時段
			updateSpecialDates(storeReq, store);

			// 刪除原有的定價計劃及其時段
			if (store.getPricingSchedules() != null) {
				store.getPricingSchedules().forEach(schedule -> {
					schedule.getTimeSlots().forEach(timeSlot -> timeSlotRepository.delete(timeSlot));
					storePricingScheduleRepository.delete(schedule);
				});
				store.getPricingSchedules().clear();
			}

			// 建立新的週間排程（支援週末獨立設定）
			List<StorePricingSchedule> schedules = createWeeklySchedules(storeReq, store);
			storePricingScheduleRepository.saveAll(schedules);
			store.getPricingSchedules().addAll(schedules);

			// 更新修改時間和修改用戶
			store.setUpdateTime(LocalDateTime.now());
			store.setUpdateUserId(id);

			// 保存並返回更新後的 store 實體
			return storeRepository.save(store);
		}).orElseThrow(() -> new Exception("Store not found with uid: " + uid));
	}

	// 輔助方法：更新特殊日期
	private void updateSpecialDates(StoreReq storeReq, Store store) {
		List<SpecialDate> specialDates = new ArrayList<>();
		if (storeReq.getSpecialDates() != null) {
			for (SpecialDateReq dateReq : storeReq.getSpecialDates()) {
				SpecialDate specialDate = new SpecialDate();
				specialDate.setDate(dateReq.getDate());
				specialDate.setOpenTime(dateReq.getOpenTime());
				specialDate.setCloseTime(dateReq.getCloseTime());
				specialDate.setRegularRate(dateReq.getRegularRate());
				specialDate.setStore(store);

				List<SpecialTimeSlot> slots = new ArrayList<>();
				for (SpecialTimeSlotReq slotReq : dateReq.getTimeSlots()) {
					SpecialTimeSlot slot = new SpecialTimeSlot();
					slot.setStartTime(slotReq.getStartTime());
					slot.setEndTime(slotReq.getEndTime());
					slot.setIsDiscount(slotReq.getIsDiscount());
					slot.setPrice(slotReq.getPrice());
					slot.setSpecialDate(specialDate);
					slots.add(slot);
				}
				specialDate.setTimeSlots(slots);
				specialDates.add(specialDate);
			}
			specialDateRepository.saveAll(specialDates);
			store.getSpecialDates().addAll(specialDates);
		}
	}
	@Transactional
	public void deleteStore(String uid) {
		Store store = storeRepository.findByUid(uid)
				.orElseThrow(() -> new RuntimeException("找不到該 Store: " + uid));

		// 清空 poolTables 的關聯
		if (store.getPoolTables() != null) {
			store.getPoolTables().forEach(table -> table.setStore(null)); // 解除關聯
			store.getPoolTables().clear(); // 清除集合
		}

		// 清空 pricingSchedules 的關聯
		if (store.getPricingSchedules() != null) {
			store.getPricingSchedules().forEach(schedule -> schedule.setStore(null));
			store.getPricingSchedules().clear();
		}

		// 清空 routers 的關聯
		if (store.getRouters() != null) {
			store.getRouters().forEach(router -> router.setStore(null));
			store.getRouters().clear();
		}

		// 解除 user 的關聯
		store.setUser(null);

		// 解除 vendor 的關聯（如果你不想移除 vendor，可略過這一行）
		store.setVendor(null);

		store.setDeleted(true);

		// 最後儲存變更
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
		StoreRes.StoreResBuilder builder = StoreRes.builder()
				.id(store.getId())
				.uid(store.getUid())
				.name(store.getName())
				.address(store.getAddress())
				.imgUrl(store.getImgUrl())
				.lat(store.getLat())
				.lon(store.getLon())
				.deposit(store.getDeposit())
				.hint(store.getHint())
				.contactPhone(store.getContactPhone())
				.bookTime(store.getBookTime())
				.cancelBookTime(store.getCancelBookTime());

		// 設置 poolTables
		if (store.getPoolTables() != null) {
			builder.poolTables(store.getPoolTables());
		}

		// 將 pricingSchedules 轉換為 StorePricingScheduleRes
		if (store.getPricingSchedules() != null) {
			builder.pricingSchedules(store.getPricingSchedules().stream()
					.map(this::convertToStorePricingScheduleRes)
					.collect(Collectors.toSet()));
		} else {
			builder.pricingSchedules(Set.of());  // 如果為 null，設為空集合
		}

		// 處理特殊日期資訊 - 檢查今天是否有特殊日期
		if (store.getSpecialDates() != null && !store.getSpecialDates().isEmpty()) {
			LocalDate today = LocalDate.now();
			Optional<SpecialDate> todaySpecialDate = store.getSpecialDates().stream()
					.filter(date -> date.getDate().equals(today.toString()))
					.findFirst();

			if (todaySpecialDate.isPresent()) {
				SpecialDate specialDate = todaySpecialDate.get();
				SpecialDateRes specialDateRes = convertToSpecialDateRes(specialDate);
				builder.specialDateRes(specialDateRes);
			}
		}

		return builder.build();
	}

	private SpecialDateRes convertToSpecialDateRes(SpecialDate specialDate) {
		SpecialDateRes.SpecialDateResBuilder builder = SpecialDateRes.builder()
				.date(String.valueOf(specialDate.getDate()))
				.openTime(specialDate.getOpenTime())
				.closeTime(specialDate.getCloseTime())
				.regularRate(specialDate.getRegularRate());

		if (specialDate.getTimeSlots() != null) {
			List<SpecialTimeSlotRes> timeSlotResList = specialDate.getTimeSlots().stream()
					.map(this::convertToSpecialTimeSlotRes)
					.collect(Collectors.toList());
			builder.timeSlots(timeSlotResList);
		}

		return builder.build();
	}

	private SpecialTimeSlotRes convertToSpecialTimeSlotRes(SpecialTimeSlot slot) {
		return SpecialTimeSlotRes.builder()
				.startTime(slot.getStartTime())
				.endTime(slot.getEndTime())
				.isDiscount(slot.getIsDiscount())
				.price(slot.getPrice())
				.build();
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


	public List<SpecialTimeSlot> splitSpecialTimeSlots(String openTime, String closeTime, List<String> timeSlots, SpecialDate specialDate) throws Exception {
		List<SpecialTimeSlot> specialTimeSlotList = new ArrayList<>();

		// 假设 openTime 和 closeTime 为 HH:mm 格式的字符串，将其转换为 LocalTime
		LocalTime open = LocalTime.parse(openTime);
		LocalTime close = LocalTime.parse(closeTime);

		// 如果时段为空或时间格式不正确，抛出异常
		if (timeSlots == null || timeSlots.isEmpty()) {
			throw new Exception("时段不能为空");
		}

		// 将时段字符串转换为 LocalTime
		List<LocalTime> timeSlotList = new ArrayList<>();
		for (String slot : timeSlots) {
			timeSlotList.add(LocalTime.parse(slot));
		}

		// 切分时段：将时段从 openTime 到 closeTime 进行切分
		for (int i = 0; i < timeSlotList.size(); i++) {
			LocalTime start = (i == 0) ? open : timeSlotList.get(i - 1);
			LocalTime end = timeSlotList.get(i);

			// 确保每个时段的结束时间都在总的 open 和 close 时间范围内
			if (end.isAfter(close)) {
				throw new Exception("时段结束时间超出了规定的关闭时间");
			}

			// 创建新的 SpecialTimeSlot 对象，并添加到列表中
			SpecialTimeSlot timeSlot = new SpecialTimeSlot();
			timeSlot.setStartTime(start);
			timeSlot.setEndTime(end);
			timeSlot.setSpecialDate(specialDate);
			specialTimeSlotList.add(timeSlot);
		}

		// 添加最后一个时段
		SpecialTimeSlot lastTimeSlot = new SpecialTimeSlot();
		lastTimeSlot.setStartTime(timeSlotList.get(timeSlotList.size() - 1));
		lastTimeSlot.setEndTime(close);
		lastTimeSlot.setSpecialDate(specialDate);
		specialTimeSlotList.add(lastTimeSlot);

		return specialTimeSlotList;
	}

}
