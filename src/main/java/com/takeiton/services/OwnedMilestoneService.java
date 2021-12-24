package com.takeiton.services;

import com.takeiton.models.*;
import com.takeiton.repositories.AppUserRepository;
import com.takeiton.repositories.MilestoneRepository;
import com.takeiton.repositories.ObjectiveRepository;
import com.takeiton.util.Status;
import org.apache.commons.math3.util.Precision;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Service
public class OwnedMilestoneService {
    public static final Predicate<Task> TASK_BY_DONE_STATUS = task -> Status.COMPLETED.toString().equalsIgnoreCase(task.getStatus());
    public static final Predicate<Task> TASK_BY_INPROGRESS_STATUS = task -> Status.IN_PROGRESS.toString().equalsIgnoreCase(task.getStatus());
    public static final Predicate<Task> TASK_BY_NOTSTARTED_STATUS = task -> Status.NOT_STARTED.toString().equalsIgnoreCase(task.getStatus());
    @Autowired
    AppUserRepository appUserRepository;
    @Autowired
    MilestoneRepository milestoneRepository;
    @Autowired
    ObjectiveRepository objectiveRepository;

    public Milestone createMilestone(Long objectiveId, Milestone milestone, String ownerName) {
        if (milestone == null) {
            throw new IllegalArgumentException("Null Milestone");
        }
        AppUser appUser = appUserRepository.findById(ownerName).get();
        Optional<Objective> objectiveByIdAndOwner = objectiveRepository.findByIdAndOwner(objectiveId, appUser);
        if (objectiveByIdAndOwner.isEmpty()) {
            throw new AccessDeniedException("Objective not found");
        }
        milestone.setOwner(appUser);
        milestone.setStatus(Status.NOT_STARTED.toString());


        Objective objective = objectiveByIdAndOwner.get();

        milestone.setParentId(objective.getClientId());
        Milestone createdMilestone = milestoneRepository.save(milestone);

        List<Milestone> objMilestones = objective.getMilestones();
        if (objMilestones == null) {
            objMilestones = new ArrayList<Milestone>();
        }
        objMilestones.add(createdMilestone);
        objective.setMilestones(objMilestones);
        objectiveRepository.save(objective);
        createdMilestone.setClientId(objective.getClientId() + "\\" + Milestone.class.getSimpleName() + "_" + createdMilestone.getId());
        createdMilestone = milestoneRepository.save(createdMilestone);
        return createdMilestone;
    }

    public Iterable<Milestone> findAll(String ownerName) {
        AppUser appUser = appUserRepository.findById(ownerName).get();

        List<Milestone> milestoneList = milestoneRepository.findAllByOwner(appUser);
        for (Milestone milestone : milestoneList) {
            updateAggregates(milestone);
        }
        return milestoneList;
    }

    public Optional<Milestone> findById(Long milestoneId, String ownerName) {
        AppUser appUser = appUserRepository.findById(ownerName).get();
        Optional<Milestone> optionalMilestone = milestoneRepository.findByIdAndOwner(milestoneId, appUser);
        if (optionalMilestone.isPresent()) {
            Milestone milestone = optionalMilestone.get();
            updateAggregates(milestone);
            optionalMilestone = Optional.of(milestone);
        }
        return optionalMilestone;
    }

    private void updateAggregates(Milestone milestone) {
        StatusAggregate taskStatusAggregate = getTaskStatusAggregate(milestone);
        milestone.setTaskStatusAggregates(taskStatusAggregate);
    }

    private StatusAggregate getTaskStatusAggregate(Milestone milestone) {
        List<Task> tasks = milestone.getTasks();
        double doneCount = tasks.stream().filter(TASK_BY_DONE_STATUS).count();
        double inProgressCount = tasks.stream().filter(TASK_BY_INPROGRESS_STATUS).count();
        double notStartedCount = tasks.stream().filter(TASK_BY_NOTSTARTED_STATUS).count();
        StatusAggregate statusAggregate = StatusAggregate.builder().build();
        long size = tasks.size();
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


    public Iterable<Milestone> findMilestonesForObjective(Long objectiveId, String ownerName) {
        AppUser appUser = appUserRepository.findById(ownerName).get();
        Optional<Objective> objectiveByIdAndOwner = objectiveRepository.findByIdAndOwner(objectiveId, appUser);
        if (objectiveByIdAndOwner.isEmpty()) {
            throw new AccessDeniedException("Objective not found");
        }
        Objective objective = objectiveByIdAndOwner.get();
        List<Milestone> objMilestones = objective.getMilestones();
        if (objMilestones == null) {
            objMilestones = new ArrayList<Milestone>();
        }
        for (Milestone milestone : objMilestones) {
            updateAggregates(milestone);
        }
        return objMilestones;
    }
}
