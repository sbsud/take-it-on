package com.takeiton.services;

import com.takeiton.models.AppUser;
import com.takeiton.models.Milestone;
import com.takeiton.models.Objective;
import com.takeiton.models.Task;
import com.takeiton.repositories.MilestoneRepository;
import com.takeiton.repositories.ObjectiveRepository;
import com.takeiton.repositories.TaskRepository;
import com.takeiton.util.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OwnedTaskService {

    @Autowired
    ObjectiveRepository objectiveRepository;

    @Autowired
    MilestoneRepository milestoneRepository;

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    UserService userService;


    public Task createTaskForObjective(Long objectiveId, Task task, String ownerName) {
        if (task == null) {
            throw new IllegalArgumentException("Null Task");
        }
        AppUser appUser = userService.getAppUserForName(ownerName);
        if (appUser == null) {
            throw new AccessDeniedException("Invalid user");
        }
        Task createdTask = createTaskWithProperties(task, appUser);
        Objective objective = objectiveRepository.findByIdAndOwner(objectiveId, appUser).get();
        List<Task> taskList = objective.getTasks();
        taskList.add(createdTask);
        objective.setTasks(taskList);
        objectiveRepository.save(objective);
        return createdTask;
    }

    public Task createTaskForMilestone(Long milestoneId, Task task, String ownerName) {
        if (task == null) {
            throw new IllegalArgumentException("Null Task");
        }
        AppUser appUser = userService.getAppUserForName(ownerName);
        if (appUser == null) {
            throw new AccessDeniedException("Invalid user");
        }
        Task createdTask = createTaskWithProperties(task, appUser);
        Milestone milestone = milestoneRepository.findByIdAndOwner(milestoneId, appUser).get();
        List<Task> taskList = milestone.getTasks();
        taskList.add(createdTask);
        milestone.setTasks(taskList);
        milestoneRepository.save(milestone);
        return createdTask;
    }

    private Task createTaskWithProperties(Task task, AppUser appUser) {
        task.setOwner(appUser);
        task.setStatus(Status.NOT_STARTED.toString());
        return taskRepository.save(task);
    }

    public Optional<Task> findById(Long taskId, String ownerName) {
        AppUser appUser = userService.getAppUserForName(ownerName);
        if (appUser == null) {
            throw new AccessDeniedException("Invalid user");
        }
        Optional<Task> optionalTask = taskRepository.findByIdAndOwner(taskId, appUser);
        if (optionalTask.isPresent()) {
            Task task = optionalTask.get();
            optionalTask = Optional.of(task);
        }
        return optionalTask;
    }

    public Optional<Task> save(Long taskId, Task task, String ownerName) {
        if (task == null) {
            throw new IllegalArgumentException("Invalid Task");
        }
        AppUser appUser = userService.getAppUserForName(ownerName);
        if (appUser == null) {
            throw new AccessDeniedException("Invalid user");
        }
        Optional<Task> retrievedOptionalTask = taskRepository.findByIdAndOwner(taskId, appUser);

        if (retrievedOptionalTask.isPresent()) {
            Task retrievedTask = retrievedOptionalTask.get();
            retrievedTask.setStatus(task.getStatus());
            retrievedTask.setDescription(task.getDescription());
            retrievedTask.setDueDate(task.getDueDate());
            retrievedTask.setDoneCriteria(task.getDoneCriteria());
            taskRepository.save(retrievedTask);
            return Optional.of(retrievedTask);
        }
        return Optional.empty();
    }

    public Iterable<Task> findAllTasks(String ownerName) {
        AppUser appUser = userService.getAppUserForName(ownerName);
        if (appUser == null) {
            throw new AccessDeniedException("Invalid user");
        }
        return taskRepository.findAllByOwner(appUser);
    }

    public Optional<List<Task>> findTasksForMilestone(Long milestoneId, String ownerName) {
        AppUser appUser = userService.getAppUserForName(ownerName);
        if (appUser == null) {
            throw new AccessDeniedException("Invalid user");
        }
        Optional<Milestone> optionalMilestone = milestoneRepository.findByIdAndOwner(milestoneId, appUser);
        if (optionalMilestone.isEmpty()) {
            return Optional.empty();
        }
        Milestone milestone = optionalMilestone.get();
        List<Task> tasks = milestone.getTasks();
        for (Task task : tasks) {
            updateParentAttributes(task, milestone);
        }
        return Optional.of(tasks);
    }

    private void updateParentAttributes(Task task, Milestone milestone) {
        String milClientId = "mil_" + milestone.getId();
        task.setClientId(milClientId + "\\tas_" + task.getId());
        task.setParentId(milClientId);
        task.setParentType(Milestone.class.getSimpleName());
    }
}
