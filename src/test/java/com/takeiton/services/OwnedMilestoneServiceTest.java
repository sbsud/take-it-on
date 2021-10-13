package com.takeiton.services;

import com.takeiton.models.AppUser;
import com.takeiton.models.Milestone;
import com.takeiton.models.Objective;
import com.takeiton.models.Task;
import com.takeiton.repositories.AppUserRepository;
import com.takeiton.repositories.MilestoneRepository;
import com.takeiton.repositories.ObjectiveRepository;
import com.takeiton.util.Status;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = OwnedMilestoneService.class, properties = "spring.main.lazy-initialization=true")
public class OwnedMilestoneServiceTest {


    @MockBean
    MilestoneRepository milestoneRepository;

    @MockBean
    AppUserRepository appUserRepository;

    @MockBean
    ObjectiveRepository objectiveRepository;

    @Autowired
    OwnedMilestoneService milestoneService;

    String name = "my_name";
    AppUser user = AppUser.builder().username(name).build();
    Milestone OWNED_MILESTONE = Milestone.builder().owner(user).build();

    @Test
    public void createMilestoneTest_HappyPath() {
        Objective OWNED_OBJECTIVE = Objective.builder().doneCriteria("Done_criteria").dueDate(new Date()).description("Description").name("Name").owner(user).build();
        Milestone milestone = Milestone.builder().name("my_milestone").build();
        Mockito.when(appUserRepository.findById(name)).thenReturn(java.util.Optional.ofNullable(user));
        Mockito.when(objectiveRepository.findByIdAndOwner(1L, user)).thenReturn(java.util.Optional.ofNullable(OWNED_OBJECTIVE));
        Mockito.when(milestoneRepository.save(milestone)).thenReturn(milestone);

        Milestone createdMilestone = milestoneService.createMilestone(1L, milestone, name);

        assertEquals(user.getUsername(), createdMilestone.getOwner().getUsername());
        assertEquals(Status.NOT_STARTED.toString(), createdMilestone.getStatus());

        assertNotNull(OWNED_OBJECTIVE.getMilestones());

        assertEquals(1, OWNED_OBJECTIVE.getMilestones().size());
        assertEquals(createdMilestone, OWNED_OBJECTIVE.getMilestones().get(0));
    }

    @Test
    public void createMilestone_nullMilestone() {
        Milestone nullMilestone = null;
        assertThrows(IllegalArgumentException.class, () -> {
           milestoneService.createMilestone(1L,nullMilestone, name);
        });
    }

    @Test
    public void createMilestone_badObjective() {
        Milestone milestone = Milestone.builder().name("my_milestone").build();
        Optional<Objective> optionalObjective = Optional.empty();
        Mockito.when(appUserRepository.findById(name)).thenReturn(java.util.Optional.ofNullable(user));
        Mockito.when(objectiveRepository.findByIdAndOwner(1L, user)).thenReturn(optionalObjective);
        assertThrows(AccessDeniedException.class, () -> {
            milestoneService.createMilestone(1L,milestone, name);
        });
    }

    @Test
    public void findByIdTest_validMilestoneNoChildren() {
        Mockito.when(appUserRepository.findById(name)).thenReturn(java.util.Optional.ofNullable(user));
        Mockito.when(milestoneRepository.findByIdAndOwner(1L, user)).thenReturn(java.util.Optional.ofNullable(OWNED_MILESTONE));
        Optional<Milestone> foundMilestone = milestoneService.findById(1L, user.getUsername());

        assertTrue(foundMilestone.isPresent());
        assertNotNull(foundMilestone.get());
    }

    @Test
    public void findbyIdTest_validMilestoneWithAllNotStartedTasks() {
        Task task_1 = Task.builder().status(Status.NOT_STARTED.toString()).build();
        Task task_2 = Task.builder().status(Status.NOT_STARTED.toString()).build();
        Task task_3 = Task.builder().status(Status.NOT_STARTED.toString()).build();
        Milestone milestoneWithTasks = Milestone.builder().doneCriteria("Done_criteria").dueDate(new Date()).description("Description").name("Name").owner(user).build();
        List tasks = new ArrayList<>(Arrays.asList(task_1, task_2, task_3));
        milestoneWithTasks.setTasks(tasks);

        Mockito.when(appUserRepository.findById(name)).thenReturn(java.util.Optional.ofNullable(user));
        Mockito.when(milestoneRepository.findByIdAndOwner(1L, user)).thenReturn(java.util.Optional.ofNullable(milestoneWithTasks));
        Optional<Milestone> optFoundObjective = milestoneService.findById(1L, user.getUsername());
        Milestone foundObjective = optFoundObjective.get();
        assertEquals(3, foundObjective.getTasks().size());

        assertEquals(1.0, foundObjective.getTaskStatusAggregates().getNotstartedAggregate());
        assertEquals(0.0, foundObjective.getTaskStatusAggregates().getDoneAggregate());
        assertEquals(0.0, foundObjective.getTaskStatusAggregates().getInprogressAggregate());
    }

    @Test
    public void findbyIdTest_validMilestoneWithStartedTasks() {
        Task task_1 = Task.builder().status(Status.NOT_STARTED.toString()).build();
        Task task_2 = Task.builder().status(Status.IN_PROGRESS.toString()).build();
        Task task_3 = Task.builder().status(Status.NOT_STARTED.toString()).build();
        Milestone milestoneWithTasks = Milestone.builder().doneCriteria("Done_criteria").dueDate(new Date()).description("Description").name("Name").owner(user).build();
        List tasks = new ArrayList<>(Arrays.asList(task_1, task_2, task_3));
        milestoneWithTasks.setTasks(tasks);

        Mockito.when(appUserRepository.findById(name)).thenReturn(java.util.Optional.ofNullable(user));
        Mockito.when(milestoneRepository.findByIdAndOwner(1L, user)).thenReturn(java.util.Optional.ofNullable(milestoneWithTasks));
        Optional<Milestone> optFoundMilestone = milestoneService.findById(1L, user.getUsername());
        Milestone foundMilestone = optFoundMilestone.get();
        assertEquals(3, foundMilestone.getTasks().size());

        assertEquals(0.67, foundMilestone.getTaskStatusAggregates().getNotstartedAggregate());
        assertEquals(0.0, foundMilestone.getTaskStatusAggregates().getDoneAggregate());
        assertEquals(0.33, foundMilestone.getTaskStatusAggregates().getInprogressAggregate());
    }

    @Test
    public void findbyIdTest_validMilestoneWithDoneTasks() {
        Task task_1 = Task.builder().status(Status.NOT_STARTED.toString()).build();
        Task task_2 = Task.builder().status(Status.IN_PROGRESS.toString()).build();
        Task task_3 = Task.builder().status(Status.COMPLETED.toString()).build();
        Milestone milestoneWithTasks = Milestone.builder().doneCriteria("Done_criteria").dueDate(new Date()).description("Description").name("Name").owner(user).build();
        List tasks = new ArrayList<>(Arrays.asList(task_1, task_2, task_3));
        milestoneWithTasks.setTasks(tasks);

        Mockito.when(appUserRepository.findById(name)).thenReturn(java.util.Optional.ofNullable(user));
        Mockito.when(milestoneRepository.findByIdAndOwner(1L, user)).thenReturn(java.util.Optional.ofNullable(milestoneWithTasks));
        Optional<Milestone> optFoundMilestone = milestoneService.findById(1L, user.getUsername());
        Milestone foundMilestone = optFoundMilestone.get();
        assertEquals(3, foundMilestone.getTasks().size());

        assertEquals(0.33, foundMilestone.getTaskStatusAggregates().getNotstartedAggregate());
        assertEquals(0.33, foundMilestone.getTaskStatusAggregates().getDoneAggregate());
        assertEquals(0.33, foundMilestone.getTaskStatusAggregates().getInprogressAggregate());
    }

    @Test
    public void findAllTest_WithTasks() {

        Task task_1 = Task.builder().status(Status.NOT_STARTED.toString()).build();
        Task task_2 = Task.builder().status(Status.IN_PROGRESS.toString()).build();
        Task task_3 = Task.builder().status(Status.IN_PROGRESS.toString()).build();
        Task task_4 = Task.builder().status(Status.IN_PROGRESS.toString()).build();
        Task task_5 = Task.builder().status(Status.COMPLETED.toString()).build();
        Task task_6 = Task.builder().status(Status.COMPLETED.toString()).build();

        List<Task> tasks = Arrays.asList(task_1, task_2, task_3, task_4, task_5, task_6);


        Milestone ownedMilestone_1 = Milestone.builder().doneCriteria("Done_criteria").dueDate(new Date()).description("Description").name("ownedMilestone_1").owner(user).build();
        ownedMilestone_1.setTasks(tasks);
        Milestone ownedMilestone_2 = Milestone.builder().doneCriteria("Done_criteria").dueDate(new Date()).description("Description").name("ownedMilestone_2").owner(user).build();
        Milestone ownedMilestone_3 = Milestone.builder().doneCriteria("Done_criteria").dueDate(new Date()).description("Description").name("ownedMilestone_3").owner(user).build();

        List<Milestone> milestones = new ArrayList<>(Arrays.asList(ownedMilestone_1, ownedMilestone_2, ownedMilestone_3));

        Mockito.when(appUserRepository.findById(name)).thenReturn(java.util.Optional.ofNullable(user));
        Mockito.when(milestoneRepository.findAllByOwner(user)).thenReturn(milestones);

        Iterable<Milestone> retrievedMilestonesItr = milestoneService.findAll(user.getUsername());
        List<Milestone> retrievedMilestones = new ArrayList<>();
        retrievedMilestonesItr.forEach(retrievedMilestones::add);
        assertEquals(3, retrievedMilestones.size());

        Optional<Milestone> optOwnedMilestone_1 = retrievedMilestones.stream().filter(milestone -> "ownedMilestone_1".equals(milestone.getName())).findFirst();
        assertTrue(optOwnedMilestone_1.isPresent());

        Milestone ownedMilestone = optOwnedMilestone_1.get();
        assertEquals(6, ownedMilestone.getTasks().size());

        assertEquals(0.17, ownedMilestone.getTaskStatusAggregates().getNotstartedAggregate());
        assertEquals(0.5, ownedMilestone.getTaskStatusAggregates().getInprogressAggregate());
        assertEquals(0.33, ownedMilestone.getTaskStatusAggregates().getDoneAggregate());

    }

    @Test
    public void findAllTest_WithTasksOtherMilestones() {

        Task task_1 = Task.builder().status(Status.NOT_STARTED.toString()).build();
        Task task_2 = Task.builder().status(Status.IN_PROGRESS.toString()).build();
        Task task_3 = Task.builder().status(Status.IN_PROGRESS.toString()).build();
        Task task_4 = Task.builder().status(Status.IN_PROGRESS.toString()).build();
        Task task_5 = Task.builder().status(Status.COMPLETED.toString()).build();
        Task task_6 = Task.builder().status(Status.COMPLETED.toString()).build();

        List<Task> tasks = Arrays.asList(task_1, task_2, task_3, task_4, task_5, task_6);


        Milestone ownedMilestone_1 = Milestone.builder().doneCriteria("Done_criteria").dueDate(new Date()).description("Description").name("ownedMilestone_1").owner(user).build();
        ownedMilestone_1.setTasks(tasks);
        Milestone ownedMilestone_2 = Milestone.builder().doneCriteria("Done_criteria").dueDate(new Date()).description("Description").name("ownedMilestone_2").owner(user).build();
        Milestone ownedMilestone_3 = Milestone.builder().doneCriteria("Done_criteria").dueDate(new Date()).description("Description").name("ownedMilestone_3").owner(user).build();

        List<Milestone> milestones = new ArrayList<>(Arrays.asList(ownedMilestone_1, ownedMilestone_2, ownedMilestone_3));

        Mockito.when(appUserRepository.findById(name)).thenReturn(java.util.Optional.ofNullable(user));
        Mockito.when(milestoneRepository.findAllByOwner(user)).thenReturn(milestones);

        Iterable<Milestone> retrievedMilestonesItr = milestoneService.findAll(user.getUsername());
        List<Milestone> retrievedMilestones = new ArrayList<>();
        retrievedMilestonesItr.forEach(retrievedMilestones::add);

        assertEquals(3, retrievedMilestones.size());

        Optional<Milestone> optOwnedMilestone = retrievedMilestones.stream().filter(milestone -> "ownedMilestone_2".equals(milestone.getName())).findFirst();
        assertTrue(optOwnedMilestone.isPresent());

        Milestone ownedMilestone = optOwnedMilestone.get();
        assertTrue(ownedMilestone.getTasks().isEmpty());

        assertEquals(0.0, ownedMilestone.getTaskStatusAggregates().getNotstartedAggregate());
        assertEquals(0.0, ownedMilestone.getTaskStatusAggregates().getInprogressAggregate());
        assertEquals(0.0, ownedMilestone.getTaskStatusAggregates().getDoneAggregate());

    }

}
