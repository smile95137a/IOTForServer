package backend.entity.job;

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
	private LocalDateTime createTime;

	@Column
	private Long createUserId;

	@Column
	private LocalDateTime updateTime;

	@Column
	private Long updateUserId;

	@Column
	private LocalDateTime lastActiveTime;

	@Column
	private String remarks;

}
