package src.main.java.com.frontend.service;

import src.main.java.com.frontend.entity.store.Store;
import src.main.java.com.frontend.enums.PoolTableStatus;
import src.main.java.com.frontend.repo.StoreRepository;
import src.main.java.com.frontend.res.store.StoreRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StoreService {

    @Autowired
    private StoreRepository storeRepository;

    public Optional<Store> getStoreById(String uid) {
        return storeRepository.findByUid(uid);
    }

    public List<Store> initCreateStore(List<Store> stores) {
        return storeRepository.saveAll(stores);
    }

    public Optional<List<StoreRes>> countAvailableAndInUseByUid(String uid){
        return storeRepository.countAvailableAndInUseByUid(uid, PoolTableStatus.AVAILABLE.name());
    }

}
