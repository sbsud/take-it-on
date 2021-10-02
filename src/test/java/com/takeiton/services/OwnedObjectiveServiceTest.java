package com.takeiton.services;

import com.takeiton.models.AppUser;
import com.takeiton.models.Milestone;
import com.takeiton.models.Objective;
import com.takeiton.models.Task;
import com.takeiton.repositories.AppUserRepository;
import com.takeiton.repositories.ObjectiveRepository;
import com.takeiton.util.Status;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = OwnedObjectiveService.class, properties = "spring.main.lazy-initialization=true")
public class OwnedObjectiveServiceTest {

    @MockBean
    ObjectiveRepository objectiveRepository;

    @Autowired
    OwnedObjectiveService objectiveService;

    @MockBean
    AppUserRepository appUserRepository;

    String name = "my_name";
    AppUser user = AppUser.builder().username(name).build();
    Objective OWNED_OBJECTIVE = Objective.builder().doneCriteria("Done_criteria").dueDate(new Date()).description("Description").name("Name").owner(user).build();
    Objective UNOWNED_OBJECTIVE = Objective.builder().doneCriteria("Done_criteria").dueDate(new Date()).description("Description").name("Name").build();

    @Test
    public void createObjectiveTest_happy_case() {
        Mockito.when(objectiveRepository.save(UNOWNED_OBJECTIVE)).thenReturn(UNOWNED_OBJECTIVE);
        Mockito.when(appUserRepository.findById(name)).thenReturn(java.util.Optional.ofNullable(user));

        Objective createdObjective = objectiveService.createObjective(UNOWNED_OBJECTIVE, name);

        assertEquals(name, createdObjective.getOwner().getUsername());
        assertEquals(Status.NOT_STARTED.toString(), createdObjective.getStatus());
    }

    @Test
    public void createObjectiveTest_null() {
        Objective nullObjective = null;
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            Objective createdObjective = objectiveService.createObjective(nullObjective, "somename");
        });
    }

    @Test
    public void createObjectiveTest_invalidUsername() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            Objective createdObjective = objectiveService.createObjective(UNOWNED_OBJECTIVE, name);
        });
    }

    @Test
    public void findByIdTest_validObjectiveNoChildren() {
        Mockito.when(appUserRepository.findById(name)).thenReturn(java.util.Optional.ofNullable(user));
        Mockito.when(objectiveRepository.findByIdAndOwner(1L, user)).thenReturn(java.util.Optional.ofNullable(OWNED_OBJECTIVE));
        Optional<Objective> foundObjective = objectiveService.findById(1L, user.getUsername());

        assertTrue(foundObjective.isPresent());
        assertNotNull(foundObjective.get());
    }

    @Test
    public void findbyIdTest_validObjectiveWithAllNotStartedMilestones() {
        Milestone milestone_1 = Milestone.builder().status(Status.NOT_STARTED.toString()).build();
        Milestone milestone_2 = Milestone.builder().status(Status.NOT_STARTED.toString()).build();
        Milestone milestone_3 = Milestone.builder().status(Status.NOT_STARTED.toString()).build();
        Objective objectiveWithMilestones = Objective.builder().doneCriteria("Done_criteria").dueDate(new Date()).description("Description").name("Name").owner(user).build();
        List milestones = new ArrayList<>(Arrays.asList(milestone_1, milestone_2, milestone_3));
        objectiveWithMilestones.setMilestones(milestones);

        Mockito.when(appUserRepository.findById(name)).thenReturn(java.util.Optional.ofNullable(user));
        Mockito.when(objectiveRepository.findByIdAndOwner(1L, user)).thenReturn(java.util.Optional.ofNullable(objectiveWithMilestones));
        Optional<Objective> optFoundObjective = objectiveService.findById(1L, user.getUsername());
        Objective foundObjective = optFoundObjective.get();
        assertEquals(3, foundObjective.getMilestones().size());

        assertEquals(1.0, foundObjective.getMilestoneStatusAggregates().getNotstartedAggregate());
        assertEquals(0.0, foundObjective.getMilestoneStatusAggregates().getDoneAggregate());
        assertEquals(0.0, foundObjective.getMilestoneStatusAggregates().getInprogressAggregate());
    }


    @Test
    public void findbyIdTest_validObjectiveWithStartedMilestones() {
        Milestone milestone_1 = Milestone.builder().status(Status.NOT_STARTED.toString()).build();
        Milestone milestone_2 = Milestone.builder().status(Status.IN_PROGRESS.toString()).build();
        Milestone milestone_3 = Milestone.builder().status(Status.NOT_STARTED.toString()).build();
        Objective objectiveWithMilestones = Objective.builder().doneCriteria("Done_criteria").dueDate(new Date()).description("Description").name("Name").owner(user).build();
        List milestones = new ArrayList<>(Arrays.asList(milestone_1, milestone_2, milestone_3));
        objectiveWithMilestones.setMilestones(milestones);

        Mockito.when(appUserRepository.findById(name)).thenReturn(java.util.Optional.ofNullable(user));
        Mockito.when(objectiveRepository.findByIdAndOwner(1L, user)).thenReturn(java.util.Optional.ofNullable(objectiveWithMilestones));
        Optional<Objective> optFoundObjective = objectiveService.findById(1L, user.getUsername());
        Objective foundObjective = optFoundObjective.get();
        assertEquals(3, foundObjective.getMilestones().size());
        assertEquals(0.67, foundObjective.getMilestoneStatusAggregates().getNotstartedAggregate());
        assertEquals(0.0, foundObjective.getMilestoneStatusAggregates().getDoneAggregate());
        assertEquals(0.33, foundObjective.getMilestoneStatusAggregates().getInprogressAggregate());
    }

    @Test
    public void findbyIdTest_validObjectiveWithDoneMilestones() {
        Milestone milestone_1 = Milestone.builder().status(Status.NOT_STARTED.toString()).build();
        Milestone milestone_2 = Milestone.builder().status(Status.IN_PROGRESS.toString()).build();
        Milestone milestone_3 = Milestone.builder().status(Status.COMPLETED.toString()).build();
        Objective objectiveWithMilestones = Objective.builder().doneCriteria("Done_criteria").dueDate(new Date()).description("Description").name("Name").owner(user).build();
        List milestones = new ArrayList<>(Arrays.asList(milestone_1, milestone_2, milestone_3));
        objectiveWithMilestones.setMilestones(milestones);

        Mockito.when(appUserRepository.findById(name)).thenReturn(java.util.Optional.ofNullable(user));
        Mockito.when(objectiveRepository.findByIdAndOwner(1L, user)).thenReturn(java.util.Optional.ofNullable(objectiveWithMilestones));
        Optional<Objective> optFoundObjective = objectiveService.findById(1L, user.getUsername());
        Objective foundObjective = optFoundObjective.get();
        assertEquals(3, foundObjective.getMilestones().size());
        assertEquals(0.33, foundObjective.getMilestoneStatusAggregates().getNotstartedAggregate());
        assertEquals(0.33, foundObjective.getMilestoneStatusAggregates().getDoneAggregate());
        assertEquals(0.33, foundObjective.getMilestoneStatusAggregates().getInprogressAggregate());
    }

    @Test
    public void findbyIdTest_validObjectiveWithAllNotStartedTasks() {
        Task task_1 = Task.builder().status(Status.NOT_STARTED.toString()).build();
        Task task_2 = Task.builder().status(Status.NOT_STARTED.toString()).build();
        Task task_3 = Task.builder().status(Status.NOT_STARTED.toString()).build();
        Objective objectiveWithTasks = Objective.builder().doneCriteria("Done_criteria").dueDate(new Date()).description("Description").name("Name").owner(user).build();
        List tasks = new ArrayList<>(Arrays.asList(task_1, task_2, task_3));
        objectiveWithTasks.setTasks(tasks);

        Mockito.when(appUserRepository.findById(name)).thenReturn(java.util.Optional.ofNullable(user));
        Mockito.when(objectiveRepository.findByIdAndOwner(1L, user)).thenReturn(java.util.Optional.ofNullable(objectiveWithTasks));
        Optional<Objective> optFoundObjective = objectiveService.findById(1L, user.getUsername());
        Objective foundObjective = optFoundObjective.get();
        assertEquals(3, foundObjective.getTasks().size());

        assertEquals(1.0, foundObjective.getTaskStatusAggregates().getNotstartedAggregate());
        assertEquals(0.0, foundObjective.getTaskStatusAggregates().getDoneAggregate());
        assertEquals(0.0, foundObjective.getTaskStatusAggregates().getInprogressAggregate());
    }

    @Test
    public void findbyIdTest_validObjectiveWithStartedTasks() {
        Task task_1 = Task.builder().status(Status.NOT_STARTED.toString()).build();
        Task task_2 = Task.builder().status(Status.IN_PROGRESS.toString()).build();
        Task task_3 = Task.builder().status(Status.NOT_STARTED.toString()).build();
        Objective objectiveWithTasks = Objective.builder().doneCriteria("Done_criteria").dueDate(new Date()).description("Description").name("Name").owner(user).build();
        List tasks = new ArrayList<>(Arrays.asList(task_1, task_2, task_3));
        objectiveWithTasks.setTasks(tasks);

        Mockito.when(appUserRepository.findById(name)).thenReturn(java.util.Optional.ofNullable(user));
        Mockito.when(objectiveRepository.findByIdAndOwner(1L, user)).thenReturn(java.util.Optional.ofNullable(objectiveWithTasks));
        Optional<Objective> optFoundObjective = objectiveService.findById(1L, user.getUsername());
        Objective foundObjective = optFoundObjective.get();
        assertEquals(3, foundObjective.getTasks().size());

        assertEquals(0.67, foundObjective.getTaskStatusAggregates().getNotstartedAggregate());
        assertEquals(0.0, foundObjective.getTaskStatusAggregates().getDoneAggregate());
        assertEquals(0.33, foundObjective.getTaskStatusAggregates().getInprogressAggregate());
    }

    @Test
    public void findbyIdTest_validObjectiveWithDoneTasks() {
        Task task_1 = Task.builder().status(Status.NOT_STARTED.toString()).build();
        Task task_2 = Task.builder().status(Status.IN_PROGRESS.toString()).build();
        Task task_3 = Task.builder().status(Status.COMPLETED.toString()).build();
        Objective objectiveWithTasks = Objective.builder().doneCriteria("Done_criteria").dueDate(new Date()).description("Description").name("Name").owner(user).build();
        List tasks = new ArrayList<>(Arrays.asList(task_1, task_2, task_3));
        objectiveWithTasks.setTasks(tasks);

        Mockito.when(appUserRepository.findById(name)).thenReturn(java.util.Optional.ofNullable(user));
        Mockito.when(objectiveRepository.findByIdAndOwner(1L, user)).thenReturn(java.util.Optional.ofNullable(objectiveWithTasks));
        Optional<Objective> optFoundObjective = objectiveService.findById(1L, user.getUsername());
        Objective foundObjective = optFoundObjective.get();
        assertEquals(3, foundObjective.getTasks().size());

        assertEquals(0.33, foundObjective.getTaskStatusAggregates().getNotstartedAggregate());
        assertEquals(0.33, foundObjective.getTaskStatusAggregates().getDoneAggregate());
        assertEquals(0.33, foundObjective.getTaskStatusAggregates().getInprogressAggregate());
    }

}
