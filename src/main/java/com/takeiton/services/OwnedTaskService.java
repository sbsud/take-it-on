package com.takeiton.services;

import com.takeiton.models.*;
import com.takeiton.repositories.HistoryRepository;
import com.takeiton.repositories.MilestoneRepository;
import com.takeiton.repositories.ObjectiveRepository;
import com.takeiton.repositories.TaskRepository;
import com.takeiton.util.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Service
public class OwnedTaskService {

    public static final Predicate<Task> TASK_BY_DONE_STATUS = task -> Status.COMPLETED.toString().equalsIgnoreCase(task.getStatus());
    public static final Predicate<Task> TASK_BY_INPROGRESS_STATUS = task -> Status.IN_PROGRESS.toString().equalsIgnoreCase(task.getStatus());
    public static final Predicate<Task> TASK_BY_NOTSTARTED_STATUS = task -> Status.NOT_STARTED.toString().equalsIgnoreCase(task.getStatus());

    @Autowired
    ObjectiveRepository objectiveRepository;

    @Autowired
    MilestoneRepository milestoneRepository;

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    UserService userService;

    @Autowired
    HistoryRepository historyRepository;


    public Task createTaskForObjective(Long objectiveId, Task task, String ownerName) {
        if (task == null) {
            throw new IllegalArgumentException("Null Task");
        }
        AppUser appUser = userService.getAppUserForName(ownerName);
        if (appUser == null) {
            throw new AccessDeniedException("Invalid user");
        }
        Objective objective = objectiveRepository.findByIdAndOwner(objectiveId, appUser).get();
        task.setParentId(objective.getClientId());
//        task.setParentObjectiveId(objective.getId());
        task.setParentObjective(objective);
        Task createdTask = createTaskWithProperties(task, appUser);
        createdTask.setClientId(objective.getClientId() + "\\" + Task.class.getSimpleName() + "_" + createdTask.getId());
        createdTask = createTaskWithProperties(createdTask, appUser);
        List<Task> taskList = objective.getTasks();
        taskList.add(createdTask);
        objective.setTasks(taskList);
        objectiveRepository.save(objective);
        History historyEntry = History.builder()
                .entityId(Long.toString(createdTask.getId()))
                .entityType(Task.class.getSimpleName())
                .category(createdTask.getCategory())
                .owner(createdTask.getOwner().getUsername())
                .time(new Date())
                .dueDate(createdTask.getDueDate())
                .event(HistoryEvents.CREATE.name())
                .build();
        historyRepository.save(historyEntry);
        historyEntry.setEvent(HistoryEvents.STATUS_CHANGE.name());
        historyEntry.setValue(createdTask.getStatus());
        historyRepository.save(historyEntry);
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

        Milestone milestone = milestoneRepository.findByIdAndOwner(milestoneId, appUser).get();
        task.setParentId(milestone.getClientId());
//        task.setParentMilestoneId(milestone.getId());
//        task.setParentObjectiveId(milestone.getParentObjectiveId());
        task.setParentMilestone(milestone);
        Task createdTask = createTaskWithProperties(task, appUser);
        createdTask.setClientId(milestone.getClientId() + "\\" + Task.class.getSimpleName() + "_" + createdTask.getId());
        createdTask = createTaskWithProperties(createdTask, appUser);
        List<Task> taskList = milestone.getTasks();
        taskList.add(createdTask);
        milestone.setTasks(taskList);
        milestoneRepository.save(milestone);
        History historyEntry = History.builder()
                .entityId(Long.toString(createdTask.getId()))
                .entityType(Task.class.getSimpleName())
                .category(createdTask.getCategory())
                .owner(createdTask.getOwner().getUsername())
                .time(new Date())
                .dueDate(createdTask.getDueDate())
                .event(HistoryEvents.CREATE.name())
                .build();
        historyRepository.save(historyEntry);
        historyEntry.setEvent(HistoryEvents.STATUS_CHANGE.name());
        historyEntry.setValue(createdTask.getStatus());
        historyRepository.save(historyEntry);
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
            boolean statusUpdate = false;
            if (task.getStatus() != null) {
                retrievedTask.setStatus(task.getStatus());
                statusUpdate = true;
            }
            if (task.getDescription() != null) retrievedTask.setDescription(task.getDescription());
            if (task.getDueDate() != null) retrievedTask.setDueDate(task.getDueDate());
            if (task.getDoneCriteria() != null) retrievedTask.setDoneCriteria(task.getDoneCriteria());
            if (task.getName() != null) retrievedTask.setName(task.getName());
            if (task.getCategory() != null) retrievedTask.setCategory(task.getCategory());
            if (task.getEffortLogged() != null) retrievedTask.setEffortLogged(retrievedTask.getEffortLogged() + task.getEffortLogged());


            taskRepository.save(retrievedTask);
            History historyEntry = History.builder()
                    .entityId(Long.toString(retrievedTask.getId()))
                    .entityType(Task.class.getSimpleName())
                    .category(retrievedTask.getCategory())
                    .owner(retrievedTask.getOwner().getUsername())
                    .time(new Date())
                    .dueDate(retrievedTask.getDueDate())
                    .event(HistoryEvents.UPDATE.name())
                    .build();
            historyRepository.save(historyEntry);
            if (statusUpdate) {
                historyEntry.setEvent(HistoryEvents.STATUS_CHANGE.name());
                historyEntry.setValue(retrievedTask.getStatus());
                historyRepository.save(historyEntry);
            }

            return Optional.of(retrievedTask);
        }
        return Optional.empty();
    }

    public Iterable<Task> findAllTasks(String ownerName, String status, String category) {
        AppUser appUser = userService.getAppUserForName(ownerName);
        if (appUser == null) {
            throw new AccessDeniedException("Invalid user");
        }
        if (status == null && category == null) {
            return taskRepository.findAllByOwner(appUser);
        } else if (status != null && category == null) {
            return taskRepository.findAllByOwnerAndStatus(appUser, status);
        } else if (status == null && category != null) {
            return taskRepository.findAllByOwnerAndCategory(appUser, category);
        }
        return taskRepository.findAllByOwnerAndStatusAndCategory(appUser, status, category);
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
        return Optional.of(tasks);
    }

    public Optional<List<Task>> findTasksForObjective(Long objectiveId, String ownerName) {
        AppUser appUser = userService.getAppUserForName(ownerName);
        if (appUser == null) {
            throw new AccessDeniedException("Invalid user");
        }
        Optional<Objective> optionalObjective = objectiveRepository.findByIdAndOwner(objectiveId, appUser);
        if (optionalObjective.isEmpty()) {
            return Optional.empty();
        }
        Objective objective = optionalObjective.get();
        List<Task> tasks = objective.getTasks();
        return Optional.of(tasks);
    }

    public StatusRollup findAllTasksStatusRollup(String ownerName) {
        AppUser appUser = userService.getAppUserForName(ownerName);

        List<Task> tasks = taskRepository.findAllByOwner(appUser);
        StatusRollup statusRollup = StatusRollup.builder()
                .inProgress(tasks.stream().filter(TASK_BY_INPROGRESS_STATUS).count())
                .notStarted(tasks.stream().filter(TASK_BY_NOTSTARTED_STATUS).count())
                .done(tasks.stream().filter(TASK_BY_DONE_STATUS).count())
                .build();

        return statusRollup;
    }
}
