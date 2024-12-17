package com.frontend.entity.channel;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Channels")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Channel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
	@Column
	private String uid;

    @Column(nullable = false)
    private String name;

    @Column
    private String lineToken;
    
    @Column
    private String lineSecret;

	@Column
	private LocalDateTime createTime;

	@Column
	private Long createUserId;

	@Column
	private LocalDateTime updateTime;

	@Column
	private Long updateUserId;


}
