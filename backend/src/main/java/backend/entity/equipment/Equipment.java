package src.main.java.backend.entity.equipment;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@MappedSuperclass  // 添加此注解，表示该类不会生成单独的数据库表
public class Equipment {

    @Column
    private String equipmentName;

    @Column
    private String status;

    @Column
    private LocalTime autoStartTime;

    @Column
    private LocalTime autoStopTime;

    @Column
    private String description;

    @Column
    private LocalDateTime createTime;

    @Column
    private String uid;

    @Column
    private Long createUserId;

    @Column
    private LocalDateTime updateTime;

    @Column
    private Long updateUserId;
}
