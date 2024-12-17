package com.frontend.config;//package backend.config;
//
//import java.text.ParseException;
//import java.time.Instant;
//import java.time.LocalDateTime;
//import java.time.ZoneId;
//import java.util.Date;
//
//import org.apache.commons.lang3.StringUtils;
//import org.quartz.CronExpression;
//import org.quartz.CronScheduleBuilder;
//import org.quartz.CronTrigger;
//import org.quartz.JobBuilder;
//import org.quartz.JobDataMap;
//import org.quartz.JobDetail;
//import org.quartz.JobKey;
//import org.quartz.Scheduler;
//import org.quartz.SchedulerException;
//import org.quartz.Trigger;
//import org.quartz.Trigger.TriggerState;
//import org.quartz.TriggerBuilder;
//import org.quartz.TriggerKey;
//import org.springframework.stereotype.Component;
//
//import com.mo.app.entity.job.ScheduledJob;
//import com.mo.app.res.job.ScheduledJobRes;
//import com.mo.app.utils.JsonUtils;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class QuartzHandler {
//
//	private final Scheduler scheduler;
//
//	public boolean start(ScheduledJobRes job, Class clazz) {
//		boolean result = true;
//		try {
//			String jobName = job.getJobName();
//			String jobGroup = job.getJobGroup();
//			TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
//			CronTrigger cronTrigger = (CronTrigger) scheduler.getTrigger(triggerKey);
//			if (null == cronTrigger) {
//				var jobData = JsonUtils.objectToMap(job);
//				JobDetail jobDetail =
//						JobBuilder.newJob(clazz)
//						.withIdentity(jobName, jobGroup)
//						.setJobData(new JobDataMap(jobData)).build();
//				cronTrigger = TriggerBuilder.newTrigger().withIdentity(jobName, jobGroup)
//						.withSchedule(CronScheduleBuilder.cronSchedule(job.getCronExpression())).build();
//				scheduler.scheduleJob(jobDetail, cronTrigger);
//				if (!scheduler.isShutdown()) {
//					scheduler.start();
//				}
//			} else {
//				cronTrigger = cronTrigger.getTriggerBuilder().withIdentity(triggerKey)
//						.withSchedule(CronScheduleBuilder.cronSchedule(job.getCronExpression())).build();
//				scheduler.rescheduleJob(triggerKey, cronTrigger);
//			}
//		} catch (SchedulerException e) {
//			log.info("新增定時任務異常：{}", e.getMessage());
//			result = false;
//		}
//		return result;
//	}
//
//	 public boolean pause(ScheduledJobRes job) {
//	     try {
//	         String jobName = job.getJobName();
//	         String jobGroup = job.getJobGroup();
//	         TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
//
//	         Trigger trigger = scheduler.getTrigger(triggerKey);
//	         if (trigger == null) {
//	             log.info("定時任務 {} 不存在，無法暫停", jobName);
//	             return false;
//	         }
//
//	         JobKey jobKey = trigger.getJobKey();
//	         scheduler.pauseJob(jobKey);
//
//	         return true;
//	     } catch (SchedulerException e) {
//	         log.error("暫停定時任務異常：{}", e.getMessage());
//	         return false;
//	     }
//	 }
//
//	public boolean restart(ScheduledJobRes job) {
//		boolean result = true;
//		try {
//			String jobName = job.getJobName();
//			String jobGroup = job.getJobGroup();
//			TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
//			Trigger trigger = scheduler.getTrigger(triggerKey);
//			scheduler.rescheduleJob(triggerKey, trigger);
//		} catch (SchedulerException e) {
//			log.info("重啟定時任務異常：{}", e.getMessage());
//			result = false;
//		}
//		return result;
//	}
//
//	public boolean trigger(ScheduledJobRes job) {
//		boolean result = true;
//		try {
//			String jobName = job.getJobName();
//			String jobGroup = job.getJobGroup();
//			TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
//			Trigger trigger = scheduler.getTrigger(triggerKey);
//			JobKey jobKey = trigger.getJobKey();
//			scheduler.triggerJob(jobKey);
//		} catch (SchedulerException e) {
//			log.info("立即執行一次異常：{}", e.getMessage());
//			result = false;
//		}
//		return result;
//	}
//
//	public boolean updateCronExpression(ScheduledJob job, String newCronExpression) {
//		boolean result = true;
//		try {
//			String jobName = job.getJobName();
//			String jobGroup = job.getJobGroup();
//			TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
//			CronTrigger cronTrigger = (CronTrigger) scheduler.getTrigger(triggerKey);
//			job.setCronExpression(newCronExpression);
//			CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression());
//			cronTrigger = cronTrigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(cronScheduleBuilder)
//					.build();
//			scheduler.rescheduleJob(triggerKey, cronTrigger);
//		} catch (SchedulerException e) {
//			log.info("修改觸發時間表達式異常：{}", e.getMessage());
//			result = false;
//		}
//		return result;
//	}
//
//
//	public LocalDateTime nextfireDate(String cronExpression) {
//		LocalDateTime localDateTime = null;
//		try {
//			if (StringUtils.isNotEmpty(cronExpression)) {
//				CronExpression ce = new CronExpression(cronExpression);
//				Date nextInvalidTimeAfter = ce.getNextInvalidTimeAfter(new Date());
//				localDateTime = Instant.ofEpochMilli(nextInvalidTimeAfter.getTime()).atZone(ZoneId.systemDefault())
//						.toLocalDateTime();
//			}
//		} catch (ParseException e) {
//			log.info("獲得下一次執行時間異常：{}", e.getMessage());
//		}
//		return localDateTime;
//	}
//
//
//	public boolean delete(ScheduledJobRes job) {
//	    try {
//	        String jobName = job.getJobName();
//	        String jobGroup = job.getJobGroup();
//	        TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
//	        Trigger trigger = scheduler.getTrigger(triggerKey);
//
//	        if (trigger == null) {
//	            log.info("定時任務 {} 不存在，無需刪除", jobName);
//	            return false;
//	        }
//
//	        JobKey jobKey = trigger.getJobKey();
//	        scheduler.pauseTrigger(triggerKey);
//	        scheduler.unscheduleJob(triggerKey);
//	        scheduler.deleteJob(jobKey);
//	        return true;
//	    } catch (SchedulerException e) {
//	        log.error("刪除定時任務異常：{}", e.getMessage());
//	        return false;
//	    }
//	}
//
//	public boolean has(ScheduledJob job) {
//	    try {
//	        if (scheduler.isShutdown()) {
//	            return false;
//	        }
//
//	        String jobName = job.getJobName();
//	        String jobGroup = job.getJobGroup();
//	        TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
//	        Trigger trigger = scheduler.getTrigger(triggerKey);
//	        return trigger != null;
//	    } catch (SchedulerException e) {
//	        log.error("判斷是否存在定時任務異常：{}", e.getMessage());
//	        return false;
//	    }
//	}
//
//	public String getStatus(ScheduledJob job) {
//	    try {
//	        String jobName = job.getJobName();
//	        String jobGroup = job.getJobGroup();
//	        TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
//	        TriggerState triggerState = scheduler.getTriggerState(triggerKey);
//	        return triggerState.toString();
//	    } catch (Exception e) {
//	        log.error("獲得定時任務狀態異常：{}", e.getMessage());
//	        return TriggerState.NONE.toString();
//	    }
//	}
//
//	public boolean startScheduler() {
//	    try {
//	        scheduler.start();
//	        return true;
//	    } catch (SchedulerException e) {
//	        log.error("啟動調度器異常：{}", e.getMessage());
//	        return false;
//	    }
//	}
//
//	public boolean standbyScheduler() {
//	    try {
//	        if (!scheduler.isShutdown()) {
//	            scheduler.standby();
//	            return true;
//	        } else {
//	            log.warn("調度器已經關閉，無法進入待機模式");
//	            return false;
//	        }
//	    } catch (SchedulerException e) {
//	        log.error("關閉調度器異常：{}", e.getMessage());
//	        return false;
//	    }
//	}
//
//	public boolean isStarted() {
//	    try {
//	        return scheduler.isStarted();
//	    } catch (SchedulerException e) {
//	        log.error("判斷調度器是否為啟動狀態異常：{}", e.getMessage());
//	        return false;
//	    }
//	}
//
//	public boolean isShutdown() {
//	    try {
//	        return scheduler.isShutdown();
//	    } catch (SchedulerException e) {
//	        log.error("判斷調度器是否為關閉狀態異常：{}", e.getMessage());
//	        return false;
//	    }
//	}
//
//	public boolean isInStandbyMode() {
//	    try {
//	        return scheduler.isInStandbyMode();
//	    } catch (SchedulerException e) {
//	        log.error("判斷調度器是否為待機狀態異常：{}", e.getMessage());
//	        return false;
//	    }
//	}
//
//}