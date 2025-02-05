package src.main.java.com.frontend.res.job;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScheduledJobRes {

	private Long id;
	private String uid;
	private String jobName;
	private String cronExpression;
	private String beanClass;
	private String status;
	private String jobGroup;
	private String jobDataMap;
	private LocalDateTime createTime;
	private Long createUserId;
	private LocalDateTime updateTime;
	private Long updateUserId;
	private LocalDateTime lastActiveTime;
	private String remarks;

}
