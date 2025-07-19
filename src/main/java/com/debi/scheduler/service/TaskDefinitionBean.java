package com.debi.scheduler.service;

import com.debi.scheduler.dto.TaskDefinition;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class TaskDefinitionBean implements Runnable{

    Logger log = LoggerFactory.getLogger(TaskDefinitionBean.class);
    @Getter
    @Setter
    private TaskDefinition taskDefinition;

    @Override
    public void run() {
        log.info("Running action: {} ", taskDefinition.getActionType());
        log.info("With Data: {}", taskDefinition.getData());
        log.info("Thread {}", Thread.currentThread().getName());
    }
}
