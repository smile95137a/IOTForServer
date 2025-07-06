package com.frontend.entity.topLog;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "send_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "is_send", nullable = false)
    private Boolean isSend;

    @Column(name = "user_date", nullable = false)
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime userDate;

    @Column(name = "user_id", nullable = false)
    private Long userId;

}