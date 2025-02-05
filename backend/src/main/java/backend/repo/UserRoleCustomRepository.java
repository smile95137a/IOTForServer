package src.main.java.backend.repo;

import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface UserRoleCustomRepository {
    void batchAddToBlacklist(List<Long> userIds, Long roleId);
}
