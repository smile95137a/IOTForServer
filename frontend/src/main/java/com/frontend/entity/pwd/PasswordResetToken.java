package com.frontend.entity.pwd;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "PasswordResetTokens")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PasswordResetToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

    @Column
    private Long userId;

    @Column
    private String token;

    @Column
    @Builder.Default
    private Boolean isActive = false;

    @Column
    @Builder.Default
    private Boolean passwordChanged = false;

	@Column
	private LocalDateTime createTime;
	
	@Column
	private LocalDateTime updateTime;


    @Column
    private LocalDateTime expireTime;
  
}
