package com.takeiton.controllers;

import com.takeiton.models.StatusRollup;
import com.takeiton.models.Task;
import com.takeiton.services.OwnedTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/")
public class TaskController {

    @Autowired
    OwnedTaskService taskService;

    @PostMapping(value = "objective/{objectiveId}/task")
    public Task createTaskForObjective(@PathVariable(value = "objectiveId") Long objectiveId, @RequestBody @Valid Task task, Principal principal) {
        return taskService.createTaskForObjective(objectiveId, task, principal.getName());
    }

    @PostMapping(value = "milestone/{milestoneId}/task")
    public Task createTaskForTask(@PathVariable(value = "milestoneId") Long milestoneId, @RequestBody @Valid Task task, Principal principal) {
        return taskService.createTaskForMilestone(milestoneId, task, principal.getName());
    }

    @GetMapping(value = "milestone/{milestoneId}/task")
    public ResponseEntity<List<Task>> findTasksForMilestone(@PathVariable(value = "milestoneId") Long milestoneId, Principal principal) {
        Optional<List<Task>> optionalTasks = taskService.findTasksForMilestone(milestoneId, principal.getName());
        return optionalTasks.map(tasks -> ResponseEntity.ok().body(tasks)).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(value = "objective/{objectiveId}/task")
    public ResponseEntity<List<Task>> findTasksForObjective(@PathVariable(value = "objectiveId") Long objectiveId, Principal principal) {
        Optional<List<Task>> optionalTasks = taskService.findTasksForObjective(objectiveId, principal.getName());
        return optionalTasks.map(tasks -> ResponseEntity.ok().body(tasks)).orElseGet(() -> ResponseEntity.notFound().build());
    }


    @GetMapping(value = "/task/{taskId}")
    public ResponseEntity<Task> getObjective(@PathVariable(value = "taskId") Long taskId, Principal principal) {
        Optional<Task> task = taskService.findById(taskId, principal.getName());
        return task.map(value -> ResponseEntity.ok().body(value)).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/task")
    public Iterable<Task> getAllTasks(Principal principal, @RequestParam(required = false) String status, @RequestParam(required = false) String category) {

        return taskService.findAllTasks(principal.getName(), status, category);
    }

    @GetMapping(value = "/task/overdue")
    public Iterable<Task> getAllOverdueTasks(Principal principal, @RequestParam(required = false) String category) {

        return taskService.findAllOverdueTasks(principal.getName(), category);
    }

    @GetMapping(value = "/task/statusRollup")
    public StatusRollup getAllTasksStatusRollup(Principal principal) {
        return taskService.findAllTasksStatusRollup(principal.getName());
    }

    @PutMapping(value = "task/{taskId}")
    public ResponseEntity<Task> updateTask(@PathVariable(value = "taskId") Long taskId, @RequestBody @Valid Task task, Principal principal) {
        Optional<Task> savedTask = taskService.save(taskId, task, principal.getName());
        return savedTask.map(value -> ResponseEntity.ok().body(value)).orElseGet(() -> ResponseEntity.notFound().build());
    }

}
