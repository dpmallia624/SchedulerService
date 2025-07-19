package com.debi.scheduler.dto;

import lombok.Data;

@Data
public class TaskDefinition {

    private String cronExpression;
    private String actionType;
    private String data;
    private String groupName;
}
