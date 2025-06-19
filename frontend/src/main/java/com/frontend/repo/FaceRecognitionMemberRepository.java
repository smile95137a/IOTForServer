package com.frontend.repo;

import com.frontend.entity.user.FaceRecognitionMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FaceRecognitionMemberRepository extends JpaRepository<FaceRecognitionMember, Long> {

    /**
     * 根據會員ID查找人臉辨識會員
     */
    @Query("SELECT f FROM FaceRecognitionMember f WHERE f.user.id = :userId")
    Optional<FaceRecognitionMember> findByUserId(@Param("userId") Long userId);

    /**
     * 根據員工編號查找
     */
    Optional<FaceRecognitionMember> findByEmployeeNo(String employeeNo);

    /**
     * 查找所有啟用的人臉辨識會員
     */
    List<FaceRecognitionMember> findByIsActiveTrue();

    /**
     * 根據會員名稱模糊搜索（關聯查詢）
     */
    @Query("SELECT f FROM FaceRecognitionMember f WHERE f.user.name LIKE %:name% AND f.isActive = true")
    List<FaceRecognitionMember> findByUserNameContainingAndIsActiveTrue(@Param("name") String name);

    /**
     * 檢查會員是否已經是人臉辨識會員
     */
    @Query("SELECT COUNT(f) > 0 FROM FaceRecognitionMember f WHERE f.user.id = :userId AND f.isActive = true")
    boolean existsByUserIdAndIsActiveTrue(@Param("userId") Long userId);
}