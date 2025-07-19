package com.debi.scheduler.dto;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskDefinitionJob implements Job {
    Logger log = LoggerFactory.getLogger(TaskDefinitionJob.class);
    @Override
    public void execute(JobExecutionContext context) {
        JobDataMap dataMap = context.getMergedJobDataMap();
        String actionType = dataMap.getString("actionType");
        String data       = dataMap.getString("data");
        // perform your action
        log.info("Running action={} data={}", actionType, data);
    }
}