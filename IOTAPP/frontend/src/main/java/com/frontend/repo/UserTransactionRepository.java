package com.frontend.repo;

import com.frontend.entity.log.UserTransaction;
import com.frontend.entity.user.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserTransactionRepository extends JpaRepository<UserTransaction, Long> {

}
