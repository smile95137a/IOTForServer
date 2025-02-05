package com.frontend.service;

import com.frontend.entity.vendor.Vendor;
import com.frontend.repo.StoreRepository;
import com.frontend.repo.VendorRepository;
import com.frontend.res.vendor.VendorRes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class VendorService {

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private StoreRepository storeRepository;

    public List<Vendor> initCreateVendor(List<Vendor> vendors) {
        return vendorRepository.saveAll(vendors);
    }

    public Optional<List<VendorRes>> countAvailablePoolTables(String uid) {
        return vendorRepository.countAvailablePoolTables(uid);
    }

}
