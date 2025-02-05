package src.main.java.backend.repo;

import src.main.java.backend.entity.vendor.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {

    Optional<Vendor> findByUid(String uid);

    void deleteByUid(String uid);

}
