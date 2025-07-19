package com.debi.scheduler.controller;

import com.debi.scheduler.dto.TaskDefinition;
import com.debi.scheduler.service.TaskDefinitionBean;
import com.debi.scheduler.service.TaskSchedulingService;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/schedule")
public class JobSchedulingController {

    @Autowired
    private TaskSchedulingService taskSchedulingService;

    @Autowired
    private TaskDefinitionBean taskDefinitionBean;

    @PostMapping(path="/taskdef", consumes = "application/json", produces="application/json")
    public String scheduleATask(@RequestBody TaskDefinition taskDefinition) {
        taskDefinitionBean.setTaskDefinition(taskDefinition);
        return taskSchedulingService.scheduleATask(UUID.randomUUID(), taskDefinitionBean, taskDefinition.getCronExpression());
    }
    @GetMapping(path = "/lists")
    public List<Map<String, Object>> getAllJobs() throws SchedulerException{
        return taskSchedulingService.getAllJobs();
    }

    @DeleteMapping(path="/taskdef/{jobId}")
    public void removeJob(@PathVariable("jobId") String jobId) {
        taskSchedulingService.removeScheduledTask(UUID.fromString(jobId));
    }
    @PostMapping("/create")
    public ResponseEntity<String> scheduleJob(@RequestBody TaskDefinition taskDef) throws SchedulerException {
        return ResponseEntity.ok(taskSchedulingService.scheduleQuartzTask(taskDef));
    }

    @DeleteMapping("/job/{jobId}/group/{groupName}")
    public ResponseEntity<String> deleteJob(@PathVariable("jobId") String jobId,
                                            @PathVariable("groupName") String groupName) throws SchedulerException {
        boolean deleted = taskSchedulingService.removeQuartzJob(jobId, groupName);
        return deleted ? ResponseEntity.ok("Deleted") : ResponseEntity.status(404).body("Not Found");
    }

    @PutMapping("/pause/job/{jobId}/group/{groupName}")
    public void pauseJob(@PathVariable("jobId") String jobId,
                         @PathVariable("groupName") String groupName) throws SchedulerException {

        taskSchedulingService.pauseQuartzJob(jobId, groupName);
    }

    @PutMapping("/resume/job/{jobId}/group/{groupName}")
    public void resumeJob(@PathVariable("jobId") String jobId,
                         @PathVariable("groupName") String groupName) throws SchedulerException {

        taskSchedulingService.resumeQuartzJob(jobId, groupName);
    }

    @PutMapping("/resumeAll")
    public void resumeAllJob() throws SchedulerException {

        taskSchedulingService.resumeAllQuartzJob();
    }
}
