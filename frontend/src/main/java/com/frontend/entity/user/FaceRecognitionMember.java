package com.frontend.entity.user;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "face_recognition_members")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FaceRecognitionMember implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 關聯到原本的會員
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonBackReference("faceRecognitionReference") // <== 加上名稱
    private User user;

    // 人臉辨識狀態
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true; // 是否啟用人臉辨識

    // 設備相關資訊（用於與你的ISAPIDeviceUtil整合）
    @Column(name = "employee_no")
    private String employeeNo; // 對應設備中的員工編號

    // 系統欄位
    @Column(name = "create_time")
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime createTime;

    @Column(name = "create_user_id")
    private Long createUserId;

    @Column(name = "update_time")
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Column(name = "update_user_id")
    private Long updateUserId;
}