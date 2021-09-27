package com.takeiton.services;

import com.takeiton.models.*;
import com.takeiton.repositories.AppUserRepository;
import com.takeiton.repositories.ObjectiveRepository;
import com.takeiton.util.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Service
public class OwnedObjectiveService {

    public static final Predicate<Milestone> MILESTONE_BY_DONE_STATUS = milestone -> Status.COMPLETED.toString().equalsIgnoreCase(milestone.getStatus());
    public static final Predicate<Milestone> MILESTONE_BY_INPROGRESS_STATUS = milestone -> Status.IN_PROGRESS.toString().equalsIgnoreCase(milestone.getStatus());
    public static final Predicate<Milestone> MILESTONE_BY_NOTSTARTED_STATUS = milestone -> Status.NOT_STARTED.toString().equalsIgnoreCase(milestone.getStatus());

    public static final Predicate<Task> TASK_BY_DONE_STATUS = task -> Status.COMPLETED.toString().equalsIgnoreCase(task.getStatus());
    public static final Predicate<Task> TASK_BY_INPROGRESS_STATUS = task -> Status.IN_PROGRESS.toString().equalsIgnoreCase(task.getStatus());
    public static final Predicate<Task> TASK_BY_NOTSTARTED_STATUS = task -> Status.NOT_STARTED.toString().equalsIgnoreCase(task.getStatus());

    @Autowired
    AppUserRepository appUserRepository;

    @Autowired
    ObjectiveRepository objectiveRepository;

    public Objective createObjective(Objective objective, String username) {
        AppUser appUser = appUserRepository.findById(username).get();
        objective.setOwner(appUser);
        objective.setStatus(Status.NOT_STARTED.toString());
        return objectiveRepository.save(objective);
    }

    public List<Objective> findAll(String ownerName, boolean rollup) {
        AppUser appUser = appUserRepository.findById(ownerName).get();

        List<Objective> objectiveList = objectiveRepository.findAllByOwner(appUser);
        for (Objective objective : objectiveList) {
            updateAggregates(objective);
        }
        return objectiveList;
    }

    public Optional<Objective> findById(Long objectiveId, String ownerName) {
        AppUser appUser = appUserRepository.findById(ownerName).get();
        Optional<Objective> optionalObjective = objectiveRepository.findByIdAndOwner(objectiveId, appUser);
        if (optionalObjective.isPresent()) {
            Objective objective = optionalObjective.get();
            updateAggregates(objective);
            optionalObjective = Optional.of(objective);
        }
        return optionalObjective;
    }

    private StatusAggregate getMilestoneStatusAggregate(Objective objective) {
        List<Milestone> milestones = objective.getMilestones();
        if (milestones.isEmpty()) {
            return null;
        }
        double doneCount = milestones.stream().filter(MILESTONE_BY_DONE_STATUS).count();
        double inProgressCount = milestones.stream().filter(MILESTONE_BY_INPROGRESS_STATUS).count();
        double notStartedCount = milestones.stream().filter(MILESTONE_BY_NOTSTARTED_STATUS).count();
        int size = milestones.size();
        double doneAggregate = doneCount / size;
        double inProgressAggregate = inProgressCount / size;
        double notStartedAggregate = notStartedCount / size;
        StatusAggregate statusAggregate = StatusAggregate.builder()
                .doneAggregate(doneAggregate)
                .inprogressAggregate(inProgressAggregate)
                .notstartedAggregate(notStartedAggregate)
                .build();
        return statusAggregate;
    }

    private void updateAggregates(Objective objective) {
        StatusAggregate milestoneStatusAggregate = getMilestoneStatusAggregate(objective);
        objective.setMilestoneStatusAggregates(milestoneStatusAggregate);
        StatusAggregate taskStatusAggregate = getTaskStatusAggregate(objective);
        objective.setTaskStatusAggregates(taskStatusAggregate);
    }

    private StatusAggregate getTaskStatusAggregate(Objective objective) {
        List<Task> tasks = objective.getTasks();
        if (tasks.isEmpty()) {
            return null;
        }
        double doneCount = tasks.stream().filter(TASK_BY_DONE_STATUS).count();
        double inProgressCount = tasks.stream().filter(TASK_BY_INPROGRESS_STATUS).count();
        double notStartedCount = tasks.stream().filter(TASK_BY_NOTSTARTED_STATUS).count();
        long size = tasks.size();
        double doneAggregate = doneCount / size;
        double inProgressAggregate = inProgressCount / size;
        double notStartedAggregate = notStartedCount / size;
        StatusAggregate statusAggregate = StatusAggregate.builder()
                .doneAggregate(doneAggregate)
                .inprogressAggregate(inProgressAggregate)
                .notstartedAggregate(notStartedAggregate)
                .build();
        return statusAggregate;
    }
}
