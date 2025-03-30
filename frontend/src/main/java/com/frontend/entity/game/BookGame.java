package com.frontend.entity.game;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class BookGame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String userUId;

    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy/MM/dd HH.mm.ss")
    private LocalDateTime startTime; // 遊戲開始時間

    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy/MM/dd HH.mm.ss")
    private LocalDateTime endTime; // 遊戲結束時間

    @Column
    private String status;

    @Column
    private String gameId;

    @Column
    private Long storeId;

    @Column
    private String storeName;

    @Column
    private Long vendorId;
    @Column
    private String vendorName;

    @Column
    private String contactInfo;

    @Column
    private Long poolTableId;

    @Column
    private String poolTableName;
}
