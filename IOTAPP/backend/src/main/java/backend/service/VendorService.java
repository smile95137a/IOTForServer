package backend.service;

import backend.entity.vendor.Vendor;
import backend.repo.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VendorService {

    @Autowired
    private VendorRepository vendorRepository;

    // Create a new vendor
    public Vendor createVendor(Vendor vendor) {
        return vendorRepository.save(vendor);
    }

    // Retrieve a vendor by ID
    public Optional<Vendor> getVendorById(Long id) {
        return vendorRepository.findById(id);
    }

    // Retrieve all vendors
    public List<Vendor> getAllVendors() {
        return vendorRepository.findAll();
    }

    // Update a vendor
    public Vendor updateVendor(Long id, Vendor updatedVendor) {
        return vendorRepository.findById(id).map(vendor -> {
            vendor.setName(updatedVendor.getName());
            vendor.setContactInfo(updatedVendor.getContactInfo());
            vendor.setStores(updatedVendor.getStores());
            vendor.setCreateTime(updatedVendor.getCreateTime());
            vendor.setCreateUserId(updatedVendor.getCreateUserId());
            vendor.setUpdateTime(updatedVendor.getUpdateTime());
            vendor.setUpdateUserId(updatedVendor.getUpdateUserId());
            return vendorRepository.save(vendor);
        }).orElseThrow(() -> new RuntimeException("Vendor not found with id: " + id));
    }

    // Delete a vendor
    public void deleteVendor(Long id) {
        vendorRepository.deleteById(id);
    }
}
