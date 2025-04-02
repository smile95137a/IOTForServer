package com.frontend.req.job;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScheduledJobReq {

	private Long id;
	private String uid;
	private String jobName;
	private String cronExpression;
	private String beanClass;
	private String status;
	private String jobGroup;
	private String jobDataMap;
	@JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
	private LocalDateTime createTime;
	private Long createUserId;
	@JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
	private LocalDateTime updateTime;
	private Long updateUserId;
	private LocalDateTime lastActiveTime;
	private String remarks;

}
