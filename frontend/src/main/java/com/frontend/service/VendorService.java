package com.frontend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

	public Vendor createVendor(VendorReq vendorReq, Long userId) {
		String s = RandomUtils.genRandom(24);
		Vendor vendor = new Vendor();
		vendor.setUid(s);
		vendor.setName(vendorReq.getName());
		vendor.setContactInfo(vendorReq.getContactInfo());
		vendor.setCreateTime(LocalDateTime.now());
		vendor.setCreateUserId(userId);

		return vendorRepository.save(vendor);
	}

	// Retrieve a vendor by ID
	public Optional<Vendor> getVendorById(String uid) {
		return vendorRepository.findByUid(uid);
	}

	// Retrieve all vendors
	public List<Vendor> getAllVendors() {
		return vendorRepository.findAll();
	}

	// Update a vendor
	public Vendor updateVendor(String uid, VendorReq vendor, Long id) {
		return vendorRepository.findByUid(uid).map(res -> {
			res.setName(vendor.getName());
			res.setContactInfo(vendor.getContactInfo());
			res.setStores(vendor.getStore());
			res.setUpdateTime(LocalDateTime.now());
			res.setUpdateUserId(id);
			return vendorRepository.save(res);
		}).orElseThrow(() -> new RuntimeException("Vendor not found with id: " + uid));
	}

	// Delete a vendor
	public void deleteVendor(String uid) {
		vendorRepository.deleteByUid(uid);
	}

	// 創建商店並與廠商關聯
	public Store addStoreToVendor(Vendor vendor, Store store) {
		store.setVendor(vendor); // 設置商店的廠商關聯
		return storeRepository.save(store); // 保存商店
	}

	// 根據廠商查詢所有商店
	public List<Store> getStoresByVendor(Vendor vendor) {
		return new ArrayList<>(vendor.getStores());
	}

	// 更新商店的廠商關聯
	public Store updateStoreVendor(Long storeId, Long vendorId) {
		Store store = storeRepository.findById(storeId)
				.orElseThrow(() -> new RuntimeException("Store not found with id: " + storeId));

		Vendor vendor = vendorRepository.findById(vendorId)
				.orElseThrow(() -> new RuntimeException("Vendor not found with id: " + vendorId));

		store.setVendor(vendor);
		return storeRepository.save(store);
	}

	// 刪除商店並解除廠商關聯
	public void deleteStore(Long storeId) {
		Store store = storeRepository.findById(storeId)
				.orElseThrow(() -> new RuntimeException("Store not found with id: " + storeId));

		store.setVendor(null); // 解除廠商與商店的關聯
		storeRepository.delete(store); // 刪除商店
	}

	// 根據ID查詢廠商
	public Optional<Vendor> getVendorById(Long vendorId) {
		return vendorRepository.findById(vendorId);
	}

}
