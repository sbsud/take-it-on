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
    @Autowired
    AppUserRepository appUserRepository;

    @Autowired
    MilestoneRepository milestoneRepository;

    @Autowired
    ObjectiveRepository objectiveRepository;
    public static final Predicate<Task> TASK_BY_DONE_STATUS = task -> Status.COMPLETED.toString().equalsIgnoreCase(task.getStatus());
    public static final Predicate<Task> TASK_BY_INPROGRESS_STATUS = task -> Status.IN_PROGRESS.toString().equalsIgnoreCase(task.getStatus());
    public static final Predicate<Task> TASK_BY_NOTSTARTED_STATUS = task -> Status.NOT_STARTED.toString().equalsIgnoreCase(task.getStatus());

    public Milestone createMilestone(Long objectiveId, Milestone milestone, String ownerName) {
        if(milestone == null) {
            throw new IllegalArgumentException("Null Milestone");
        }
        AppUser appUser = appUserRepository.findById(ownerName).get();
        Optional<Objective> objectiveByIdAndOwner = objectiveRepository.findByIdAndOwner(objectiveId, appUser);
        if (objectiveByIdAndOwner.isEmpty()) {
            throw new AccessDeniedException("Objective not found");
        }
        milestone.setOwner(appUser);
        milestone.setStatus(Status.NOT_STARTED.toString());
        Milestone createdMilestone = milestoneRepository.save(milestone);

        Objective objective = objectiveByIdAndOwner.get();
        List<Milestone> objMilestones = objective.getMilestones();
        if (objMilestones ==  null) {
            objMilestones = new ArrayList<Milestone>();
        }
        objMilestones.add(createdMilestone);
        objective.setMilestones(objMilestones);
        objectiveRepository.save(objective);
        return createdMilestone;
    }

    public Iterable<Milestone> findAll(String ownerName, boolean rollup) {
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
        if (tasks == null || tasks.isEmpty()) {
            return new StatusAggregate();
        }
        double doneCount = tasks.stream().filter(TASK_BY_DONE_STATUS).count();
        double inProgressCount = tasks.stream().filter(TASK_BY_INPROGRESS_STATUS).count();
        double notStartedCount = tasks.stream().filter(TASK_BY_NOTSTARTED_STATUS).count();
        long size = tasks.size();
        double doneAggregate = doneCount / size;
        double inProgressAggregate = inProgressCount / size;
        double notStartedAggregate = notStartedCount / size;
        StatusAggregate statusAggregate = StatusAggregate.builder()
                .doneAggregate(Precision.round(doneAggregate,2))
                .inprogressAggregate(Precision.round(inProgressAggregate,2))
                .notstartedAggregate(Precision.round(notStartedAggregate,2))
                .build();
        return statusAggregate;
    }


}
