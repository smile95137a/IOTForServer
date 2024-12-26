package com.frontend.mapper;//package com.frontend.mapper;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.stream.Collectors;
//
//import org.springframework.stereotype.Component;
//
//import com.mo.app.entity.job.ScheduledJob;
//import com.mo.app.req.job.ScheduledJobReq;
//import com.mo.app.res.job.ScheduledJobRes;
//import com.mo.app.utils.RandomUtils;
//import com.mo.app.utils.SecurityUtils;
//
//@Component
//public class ScheduledJobMapper {
//
//	public ScheduledJob mapToScheduledJob(ScheduledJobReq scheduledJobReq) {
//		var now = LocalDateTime.now();
//		var userId = SecurityUtils.getSecurityUser().getId();
//
//
//		return ScheduledJob.builder().uid(RandomUtils.genRandom(32)).jobName(scheduledJobReq.getJobName())
//				.cronExpression(scheduledJobReq.getCronExpression()).beanClass(scheduledJobReq.getBeanClass())
//				.status(scheduledJobReq.getStatus()).jobGroup(scheduledJobReq.getJobGroup())
//				.jobDataMap(scheduledJobReq.getJobDataMap()).createTime(now)
//				.createUserId(userId)
//				.remarks(scheduledJobReq.getRemarks()).build();
//	}
//
//	public ScheduledJobRes mapToScheduledJobRes(ScheduledJob scheduledJob) {
//		return ScheduledJobRes.builder().uid(scheduledJob.getUid()).jobName(scheduledJob.getJobName())
//				.cronExpression(scheduledJob.getCronExpression()).beanClass(scheduledJob.getBeanClass())
//				.status(scheduledJob.getStatus()).jobGroup(scheduledJob.getJobGroup())
//				.jobDataMap(scheduledJob.getJobDataMap()).createTime(scheduledJob.getCreateTime())
//				.createUserId(scheduledJob.getCreateUserId()).updateTime(scheduledJob.getUpdateTime())
//				.updateUserId(scheduledJob.getUpdateUserId()).lastActiveTime(scheduledJob.getLastActiveTime())
//				.remarks(scheduledJob.getRemarks()).build();
//	}
//
//	public List<ScheduledJob> mapToScheduledJobList(List<ScheduledJobReq> scheduledJobReqs) {
//		return scheduledJobReqs.stream().map(this::mapToScheduledJob).collect(Collectors.toList());
//	}
//
//	public List<ScheduledJobRes> mapToScheduledJobResList(List<ScheduledJob> scheduledJobs) {
//		return scheduledJobs.stream().map(this::mapToScheduledJobRes).collect(Collectors.toList());
//	}
//
//}
