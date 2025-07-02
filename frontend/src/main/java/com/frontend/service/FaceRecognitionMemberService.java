package com.frontend.service;

import com.frontend.entity.store.Store;
import com.frontend.entity.user.FaceRecognitionMember;
import com.frontend.entity.user.User;
import com.frontend.repo.FaceRecognitionMemberRepository;
import com.frontend.repo.RouterRepository;
import com.frontend.repo.StoreRepository;
import com.frontend.repo.UserRepository;
import com.frontend.utils.ISAPIDeviceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FaceRecognitionMemberService {

    @Autowired
    private FaceRecognitionMemberRepository faceRecognitionMemberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoreRepository storeRepository;

    private String host;
    @Autowired
    private RouterRepository routerRepository;

    /**
     * 創建人臉辨識會員
     * @param userId 會員ID
     * @param currentUserId 操作者ID
     * @return 創建結果
     */
    @Transactional
    public FaceRecognitionMember createFaceRecognitionMember(Long userId, Long currentUserId) throws Exception {
        // 檢查會員是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("會員不存在"));

        // 檢查是否已經是人臉辨識會員
        if (user.getFaceRecognitionMember() != null) {
            throw new RuntimeException("該會員已經是人臉辨識會員");
        }

        // 生成員工編號（可以使用會員ID或自定義規則）
        String employeeNo = "EMP" + String.format("%08d", userId);

        // 創建人臉辨識會員記錄
        FaceRecognitionMember faceRecognitionMember = FaceRecognitionMember.builder()
                .user(user)
                .employeeNo(employeeNo)
                .isActive(true)
                .createTime(LocalDateTime.now())
                .createUserId(currentUserId)
                .build();

        // 保存到數據庫
        faceRecognitionMember = faceRecognitionMemberRepository.save(faceRecognitionMember);

        // 可選：同步到門禁設備
         syncToDevice(faceRecognitionMember);

        return faceRecognitionMember;
    }

    /**
     * 根據會員ID搜尋人臉辨識會員
     * @param userId 會員ID
     * @return 人臉辨識會員資料
     */
    public Optional<FaceRecognitionMember> findByUserId(Long userId) {
        return faceRecognitionMemberRepository.findByUserId(userId);
    }

    /**
     * 根據員工編號搜尋
     * @param employeeNo 員工編號
     * @return 人臉辨識會員資料
     */
    public Optional<FaceRecognitionMember> findByEmployeeNo(String employeeNo) {
        return faceRecognitionMemberRepository.findByEmployeeNo(employeeNo);
    }

    /**
     * 獲取所有啟用的人臉辨識會員
     * @return 人臉辨識會員列表
     */
    public List<FaceRecognitionMember> findAllActive() {
        return faceRecognitionMemberRepository.findByIsActiveTrue();
    }

    /**
     * 停用人臉辨識會員
     * @param userId 會員ID
     * @param currentUserId 操作者ID
     */
    @Transactional
    public void deactivateFaceRecognitionMember(Long userId, Long currentUserId) {
        FaceRecognitionMember member = faceRecognitionMemberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("人臉辨識會員不存在"));

        member.setIsActive(false);
        member.setUpdateTime(LocalDateTime.now());
        member.setUpdateUserId(currentUserId);

        faceRecognitionMemberRepository.save(member);
    }

    /**
     * 檢查會員是否為人臉辨識會員
     * @param userId 會員ID
     * @return 是否為人臉辨識會員
     */
    public boolean isFaceRecognitionMember(Long userId) {
        return faceRecognitionMemberRepository.findByUserId(userId)
                .map(FaceRecognitionMember::getIsActive)
                .orElse(false);
    }

    /**
     * 同步到門禁設備（可選實現）
     * @param member 人臉辨識會員
     */
    private void syncToDevice(FaceRecognitionMember member) throws Exception {
        List<Store> all = storeRepository.findAll();
            for(Store store : all){
                // 使用你的 ISAPIDeviceUtil
                ISAPIDeviceUtil.DeviceConfig deviceConfig = new ISAPIDeviceUtil.DeviceConfig(
                        store.getStoreIP(), // 設備IP
                        "80",             // 設備端口
                        "admin",          // 設備用戶名
                        "Handsome0202@"        // 設備密碼
                );

                ISAPIDeviceUtil.UserInfo userInfo = new ISAPIDeviceUtil.UserInfo(
                        member.getEmployeeNo(),
                        member.getUser().getName(),
                        "123456" // 默認密碼
                );

                ISAPIDeviceUtil.ApiResponse response = ISAPIDeviceUtil.addUser(deviceConfig, userInfo, null);

                if (!response.isSuccess()) {
                    // 記錄同步失敗，但不影響主流程
                    System.err.println("同步到設備失敗: " + response.getErrorMessage());
                }
            }

    }
}