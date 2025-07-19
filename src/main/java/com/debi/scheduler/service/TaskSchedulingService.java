package com.debi.scheduler.service;

import com.debi.scheduler.dto.TaskDefinition;
import com.debi.scheduler.dto.TaskDefinitionJob;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ScheduledFuture;

@Service
public class TaskSchedulingService {
    Logger log = LoggerFactory.getLogger(TaskSchedulingService.class);
    @Autowired
    //private TaskScheduler taskScheduler;
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    Map<UUID, ScheduledFuture<?>> jobsMap = new HashMap<>();

    @Autowired
    private Scheduler scheduler;

    public String scheduleATask(UUID jobId, Runnable tasklet, String cronExpression) {
        log.info("Scheduling task with job id: {}  and cron expression: {} ", jobId, cronExpression);
        ScheduledFuture<?> scheduledTask = threadPoolTaskScheduler.schedule(tasklet,
                new CronTrigger(cronExpression,
                        TimeZone.getTimeZone(TimeZone.getDefault().getID())));
        jobsMap.put(jobId, scheduledTask);
        return jobId.toString();
    }

    public void removeScheduledTask(UUID jobId) {
        ScheduledFuture<?> scheduledTask = jobsMap.get(jobId);
        if(scheduledTask != null) {
            scheduledTask.cancel(true);
            jobsMap.put(jobId, null);
        }
    }

    public List<TaskDefinition> getScheduleTasks(){
        return null;
    }

    public String scheduleQuartzTask(TaskDefinition taskDef) throws SchedulerException {
        String jobId = UUID.randomUUID().toString();

        JobDetail jobDetail = JobBuilder.newJob(TaskDefinitionJob.class)
                .withIdentity(jobId, taskDef.getGroupName() == null ? "default": taskDef.getGroupName()) // group by actionType if helpful
                .usingJobData("actionType", taskDef.getActionType())
                .usingJobData("data", taskDef.getData())
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("trigger-" + jobId, "triggers")
                .withSchedule(CronScheduleBuilder
                        .cronSchedule(taskDef.getCronExpression())
                        .inTimeZone(TimeZone.getDefault())
                        .withMisfireHandlingInstructionFireAndProceed())
                .forJob(jobDetail)
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
        return jobId;
    }

    public boolean removeQuartzJob(String jobId, String group) throws SchedulerException {
        return scheduler.deleteJob(JobKey.jobKey(jobId, group));
    }

    public List<Map<String, Object>> getAllJobs() throws SchedulerException {
        List<Map<String, Object>> jobList = new ArrayList<>();

        for (String groupName : scheduler.getJobGroupNames()) {
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                Map<String, Object> jobData = new HashMap<>();
                JobDetail jobDetail = scheduler.getJobDetail(jobKey);

                jobData.put("jobId", jobKey.getName());
                jobData.put("groupName", jobKey.getGroup());
                jobData.put("description", jobDetail.getDescription());

                List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                for (Trigger trigger : triggers) {
                    jobData.put("nextFireTime", trigger.getNextFireTime());
                    jobData.put("previousFireTime", trigger.getPreviousFireTime());
                    jobData.put("triggerState", scheduler.getTriggerState(trigger.getKey()).name());
                    jobData.put("cronExpression", (trigger instanceof CronTriggerImpl)
                            ? ((CronTriggerImpl) trigger).getCronExpression()
                            : "N/A");
                }

                jobList.add(jobData);
            }
        }

        return jobList;
    }

    public void pauseQuartzJob(String jobId, String group) throws SchedulerException {
        scheduler.pauseJob(JobKey.jobKey(jobId, group));
    }

    public void resumeQuartzJob(String jobId, String group) throws SchedulerException {
        scheduler.resumeJob(JobKey.jobKey(jobId, group));
    }

    public void resumeAllQuartzJob() throws SchedulerException {
        scheduler.resumeAll();
    }
}
