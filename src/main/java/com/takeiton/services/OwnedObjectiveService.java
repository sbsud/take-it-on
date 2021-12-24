package com.takeiton.services;

import com.takeiton.models.*;
import com.takeiton.repositories.AppUserRepository;
import com.takeiton.repositories.ObjectiveRepository;
import com.takeiton.util.Status;
import org.apache.commons.math3.util.Precision;
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
        if (objective == null) {
            throw new IllegalArgumentException("Objective is null");
        }
        Optional<AppUser> optAppUser = appUserRepository.findById(username);
        AppUser appUser;
        if (optAppUser.isPresent()) {
            appUser = optAppUser.get();
        } else {
            throw new IllegalArgumentException("Invalid username");
        }

        objective.setOwner(appUser);
        objective.setStatus(Status.NOT_STARTED.toString());
        objective =objectiveRepository.save(objective);
        objective.setClientId(Objective.class.getSimpleName()+"_"+objective.getId());
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
        double doneCount = milestones.stream().filter(MILESTONE_BY_DONE_STATUS).count();
        double inProgressCount = milestones.stream().filter(MILESTONE_BY_INPROGRESS_STATUS).count();
        double notStartedCount = milestones.stream().filter(MILESTONE_BY_NOTSTARTED_STATUS).count();
        StatusAggregate statusAggregate = StatusAggregate.builder().build();
        int size = milestones.size();
        if (size > 0) {
            double doneAggregate = doneCount / size;
            double inProgressAggregate = inProgressCount / size;
            double notStartedAggregate = notStartedCount / size;
            statusAggregate.setDoneAggregate(Precision.round(doneAggregate, 2));
            statusAggregate.setInprogressAggregate(Precision.round(inProgressAggregate, 2));
            statusAggregate.setNotstartedAggregate(Precision.round(notStartedAggregate, 2));
        }
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
        double doneCount = tasks.stream().filter(TASK_BY_DONE_STATUS).count();
        double inProgressCount = tasks.stream().filter(TASK_BY_INPROGRESS_STATUS).count();
        double notStartedCount = tasks.stream().filter(TASK_BY_NOTSTARTED_STATUS).count();
        long size = tasks.size();
        StatusAggregate statusAggregate = StatusAggregate.builder().build();
        if (size > 0 ) {
            double doneAggregate = doneCount / size;
            double inProgressAggregate = inProgressCount / size;
            double notStartedAggregate = notStartedCount / size;
            statusAggregate.setDoneAggregate(Precision.round(doneAggregate, 2));
            statusAggregate.setInprogressAggregate(Precision.round(inProgressAggregate, 2));
            statusAggregate.setNotstartedAggregate(Precision.round(notStartedAggregate, 2));
        }
        return statusAggregate;
    }


    public Optional<Objective> save(Long objectiveId, Objective objective, String ownerName) {
        if (objective == null) {
            throw new IllegalArgumentException("Invalid objective");
        }
        Optional<AppUser> optAppUser = appUserRepository.findById(ownerName);
        AppUser appUser;
        if (optAppUser.isPresent()) {
            appUser = optAppUser.get();
        } else {
            throw new IllegalArgumentException("Invalid username");
        }
        Optional<Objective> retrievedOptionalObjective = objectiveRepository.findByIdAndOwner(objectiveId, appUser);

        if (retrievedOptionalObjective.isPresent()) {
            Objective retrievedObjective = retrievedOptionalObjective.get();
            retrievedObjective.setStatus(objective.getStatus());
            retrievedObjective.setDescription(objective.getDescription());
            retrievedObjective.setDueDate(objective.getDueDate());
            retrievedObjective.setDoneCriteria(objective.getDoneCriteria());
            retrievedObjective.setName(objective.getName());
            objectiveRepository.save(retrievedObjective);
            return Optional.of(retrievedObjective);
        }
        return Optional.empty();
    }
}
