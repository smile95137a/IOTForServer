package com.frontend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.frontend.entity.user.User;
import com.frontend.repo.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.frontend.controller.admin.VendorReq;
import com.frontend.entity.store.Store;
import com.frontend.entity.vendor.Vendor;
import com.frontend.repo.StoreRepository;
import com.frontend.repo.VendorRepository;
import com.frontend.utils.RandomUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class VendorService {
	private final VendorRepository vendorRepository;
	private final StoreRepository storeRepository;
	private final UserRepository userRepository;

	// **創建 Vendor 並關聯 User**
	public Vendor createVendor(VendorReq vendorReq, Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

		Vendor vendor = new Vendor();
		vendor.setUid(RandomUtils.genRandom(24));
		vendor.setName(vendorReq.getName());
		vendor.setContactInfo(vendorReq.getContactInfo());
		vendor.setCreateTime(LocalDateTime.now());
		vendor.setUpdateTime(LocalDateTime.now());
		vendor.setCreateUserId(userId);
		vendor.setUpdateUserId(userId);
		vendor.setUserId(vendorReq.getUserId());

		// **將 User 連結到 Vendor**
		user.setVendor(vendor);
		vendor.getUsers().add(user);

		Vendor savedVendor = vendorRepository.save(vendor);
		userRepository.save(user); // 確保 user 的 vendor_id 正確設置

		return savedVendor;
	}

	// **根據 UID 獲取 Vendor**
	public Optional<Vendor> getVendorByUid(String uid) {
		return vendorRepository.findByUid(uid);
	}

	// **獲取所有 Vendor**
	public List<Vendor> getAllVendors() {
		return vendorRepository.findAll();
	}

	// **更新 Vendor，確保 `userId` 也能更新**
	public Vendor updateVendor(String uid, VendorReq vendorReq, Long userId) {
		return vendorRepository.findByUid(uid).map(vendor -> {
			vendor.setName(vendorReq.getName());
			vendor.setContactInfo(vendorReq.getContactInfo());
			vendor.setUpdateTime(LocalDateTime.now());
			vendor.setUpdateUserId(userId);
			vendor.setUserId(vendorReq.getUserId()); // 確保更新時 `userId` 也正確

			return vendorRepository.save(vendor);
		}).orElseThrow(() -> new RuntimeException("Vendor not found with UID: " + uid));
	}

	// **刪除 Vendor，並解除所有關聯的 User**
	@Transactional
	public void deleteVendor(String uid) {
		Vendor vendor = vendorRepository.findByUid(uid)
				.orElseThrow(() -> new RuntimeException("Vendor not found with UID: " + uid));

		// **解除 User 關聯**
		Set<User> users = vendor.getUsers();
		for (User user : users) {
			user.setVendor(null);
			userRepository.save(user);
		}

		vendorRepository.delete(vendor);
	}

	// **獲取 Vendor 下的所有 Store**
	public List<Store> getStoresByVendor(Vendor vendor) {
		return storeRepository.findByVendor(vendor);
	}

	// **為 Vendor 添加 Store**
	public Store addStoreToVendor(Long vendorId, Store store) {
		Vendor vendor = vendorRepository.findById(vendorId)
				.orElseThrow(() -> new RuntimeException("Vendor not found with ID: " + vendorId));

		store.setVendor(vendor);
		return storeRepository.save(store);
	}

	// **更新 Store 的 Vendor**
	public Store updateStoreVendor(Long storeId, Long vendorId) {
		Store store = storeRepository.findById(storeId)
				.orElseThrow(() -> new RuntimeException("Store not found with ID: " + storeId));

		Vendor vendor = vendorRepository.findById(vendorId)
				.orElseThrow(() -> new RuntimeException("Vendor not found with ID: " + vendorId));

		store.setVendor(vendor);
		return storeRepository.save(store);
	}

	// **刪除 Store**
	public void deleteStore(Long storeId) {
		Store store = storeRepository.findById(storeId)
				.orElseThrow(() -> new RuntimeException("Store not found with ID: " + storeId));
		storeRepository.delete(store);
	}
}
