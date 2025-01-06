package backend.repo.impl;

import backend.repo.UserRoleCustomRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.List;

public class UserRoleCustomRepositoryImpl implements UserRoleCustomRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void batchAddToBlacklist(List<Long> userIds, Long roleId) {
        StringBuilder sql = new StringBuilder("INSERT INTO user_roles (user_id, role_id) VALUES ");
        for (int i = 0; i < userIds.size(); i++) {
            sql.append("(").append(userIds.get(i)).append(", ").append(roleId).append(")");
            if (i < userIds.size() - 1) {
                sql.append(", ");
            }
        }
        entityManager.createNativeQuery(sql.toString()).executeUpdate();
    }
}
