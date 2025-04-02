package com.frontend.entity.job;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "scheduled_job")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScheduledJob {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column
	private Long id;
	
	@Column
	private String uid;

	@Column
	private String jobName;

	@Column
	private String cronExpression;

	@Column
	private String beanClass;

	@Column
	private String status;

	@Column
	private String jobGroup;

	@Column
	private String jobDataMap;

	@Column
	@JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
	private LocalDateTime createTime;

	@Column
	private Long createUserId;

	@Column
	@JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
	private LocalDateTime updateTime;

	@Column
	private Long updateUserId;

	@Column
	private LocalDateTime lastActiveTime;

	@Column
	private String remarks;

}
