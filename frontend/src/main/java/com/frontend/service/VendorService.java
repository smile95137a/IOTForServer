package com.frontend.service;

import java.time.LocalDateTime;
import java.util.*;

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

	// **创建Vendor并关联User**
	public Vendor createVendor(VendorReq vendorReq, Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

		String s = RandomUtils.genRandom(24);
		Vendor vendor = new Vendor();
		vendor.setUid(s);
		vendor.setName(vendorReq.getName());
		vendor.setContactInfo(vendorReq.getContactInfo());
		vendor.setCreateTime(LocalDateTime.now());
		vendor.setCreateUserId(userId);

		// **将 User 关联到 Vendor**
		user.setVendor(vendor);
		vendor.getUsers().add(user);

		// **先保存 Vendor，再保存 User**
		Vendor savedVendor = vendorRepository.save(vendor);
		userRepository.save(user);

		return savedVendor;
	}

	// **根据 UID 获取 Vendor**
	public Optional<Vendor> getVendorByUid(String uid) {
		return vendorRepository.findByUid(uid);
	}

	// **获取所有 Vendor**
	public List<Vendor> getAllVendors() {
		return vendorRepository.findAll();
	}

	// **更新 Vendor，并保持关联 User**
	public Vendor updateVendor(String uid, VendorReq vendorReq, Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
		return vendorRepository.findByUid(uid).map(vendor -> {
			vendor.setName(vendorReq.getName());
			vendor.setContactInfo(vendorReq.getContactInfo());
			vendor.setUpdateTime(LocalDateTime.now());
			vendor.setUpdateUserId(userId);
			user.setVendor(vendor);
			vendor.getUsers().add(user);
			return vendorRepository.save(vendor);
		}).orElseThrow(() -> new RuntimeException("Vendor not found with id: " + uid));
	}
	// **删除 Vendor，并解除所有 User 的关联**
	@Transactional
	public void deleteVendor(String uid) {
		Vendor vendor = vendorRepository.findByUid(uid)
				.orElseThrow(() -> new RuntimeException("Vendor not found with id: " + uid));

		// **解除 User 关联**
		for (User user : vendor.getUsers()) {
			user.setVendor(null);
			userRepository.save(user);
		}

		vendorRepository.delete(vendor);
	}

	// **获取 Vendor 下的所有 Store**
	public List<Store> getStoresByVendor(Vendor vendor) {
		return storeRepository.findByVendor(vendor);
	}

	// **根据 Vendor 添加 Store**
	public Store addStoreToVendor(Vendor vendor, Store store) {
		store.setVendor(vendor);
		return storeRepository.save(store);
	}

	// **更新 Store 的 Vendor**
	public Store updateStoreVendor(Long storeId, Long vendorId) {
		Store store = storeRepository.findById(storeId)
				.orElseThrow(() -> new RuntimeException("Store not found with id: " + storeId));

		Vendor vendor = vendorRepository.findById(vendorId)
				.orElseThrow(() -> new RuntimeException("Vendor not found with id: " + vendorId));

		store.setVendor(vendor);
		return storeRepository.save(store);
	}

	// **删除 Store**
	public void deleteStore(Long storeId) {
		Store store = storeRepository.findById(storeId)
				.orElseThrow(() -> new RuntimeException("Store not found with id: " + storeId));
		storeRepository.delete(store);
	}

}
