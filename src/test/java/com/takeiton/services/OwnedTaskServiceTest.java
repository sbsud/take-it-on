package com.takeiton.services;

import com.takeiton.models.AppUser;
import com.takeiton.models.Milestone;
import com.takeiton.models.Objective;
import com.takeiton.models.Task;
import com.takeiton.repositories.AppUserRepository;
import com.takeiton.repositories.MilestoneRepository;
import com.takeiton.repositories.ObjectiveRepository;
import com.takeiton.repositories.TaskRepository;
import com.takeiton.util.Status;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = OwnedTaskService.class, properties = "spring.main.lazy-initialization=true")
public class OwnedTaskServiceTest {

    @Autowired
    OwnedTaskService ownedTaskService;

    @MockBean
    AppUserRepository appUserRepository;

    @MockBean
    ObjectiveRepository objectiveRepository;

    @MockBean
    TaskRepository taskRepository;

    @MockBean
    MilestoneRepository milestoneRepository;

    @MockBean
    UserService userService;

    String name = "my_name";
    AppUser user = AppUser.builder().username(name).build();

    @Test
    public void createTaskForObjectiveTest_HappyPath() {
        Mockito.when(userService.getAppUserForName(name)).thenReturn(user);

        Task task = Task.builder().name("my_task").description("my tas desc").doneCriteria("task done cri").dueDate(new Date()).build();

        Mockito.when(taskRepository.save(task)).thenReturn(task);
        Objective objective = Objective.builder().name("my objective").build();
        Mockito.when(objectiveRepository.findByIdAndOwner(1L, user)).thenReturn(java.util.Optional.ofNullable(objective));
        Mockito.when(objectiveRepository.save(objective)).thenReturn(objective);

        Task savedTask = ownedTaskService.createTaskForObjective(1L, task, name);

        assertEquals(user, task.getOwner());
        assertEquals(Status.NOT_STARTED.toString(), task.getStatus());
        assertEquals("my_task", objective.getTasks().get(0).getName());
    }

    @Test
    public void createTaskForObjective_nullTask() {
        Task nullTask = null;
        assertThrows(IllegalArgumentException.class, () -> {
            ownedTaskService.createTaskForObjective(1L, nullTask, name);
        });
    }

    @Test
    public void createTaskForObjective_invalidUserTask() {
        Task task = Task.builder().name("my_task").description("my tas desc").doneCriteria("task done cri").dueDate(new Date()).build();
        Mockito.when(userService.getAppUserForName(name)).thenReturn(null);

        assertThrows(AccessDeniedException.class, () -> {
            ownedTaskService.createTaskForObjective(1L, task, name);
        });
    }

    @Test
    public void createTaskForMilestoneTest_HappyPath() {
        Mockito.when(userService.getAppUserForName(name)).thenReturn(user);

        Task task = Task.builder().name("my_task").description("my tas desc").doneCriteria("task done cri").dueDate(new Date()).build();

        Mockito.when(taskRepository.save(task)).thenReturn(task);
        Milestone milestone = Milestone.builder().name("my milestone").build();
        Mockito.when(milestoneRepository.findByIdAndOwner(1L, user)).thenReturn(java.util.Optional.ofNullable(milestone));
        Mockito.when(milestoneRepository.save(milestone)).thenReturn(milestone);

        Task savedTask = ownedTaskService.createTaskForMilestone(1L, task, name);

        assertEquals(user, task.getOwner());
        assertEquals(Status.NOT_STARTED.toString(), task.getStatus());
        assertEquals("my_task", milestone.getTasks().get(0).getName());
    }

    @Test
    public void createTaskForMilestone_nullTask() {
        Task nullTask = null;
        assertThrows(IllegalArgumentException.class, () -> {
            ownedTaskService.createTaskForMilestone(1L, nullTask, name);
        });
    }

    @Test
    public void createTaskForMilestone_invalidUserTask() {
        Task task = Task.builder().name("my_task").description("my tas desc").doneCriteria("task done cri").dueDate(new Date()).build();
        Mockito.when(userService.getAppUserForName(name)).thenReturn(null);

        assertThrows(AccessDeniedException.class, () -> {
            ownedTaskService.createTaskForMilestone(1L, task, name);
        });
    }

    @Test
    public void findTaskByIdTest() {
        Mockito.when(userService.getAppUserForName(name)).thenReturn(user);

        Task task = Task.builder().name("my_task").description("my tas desc").doneCriteria("task done cri").dueDate(new Date()).build();
        Mockito.when(taskRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(task));

        Optional<Task> optRetrievedTask = ownedTaskService.findById(1L, name);

        assertTrue(optRetrievedTask.isPresent());
        assertEquals("my_task", optRetrievedTask.get().getName());
    }

    @Test
    public void findTaskByIdTest_invalidUser() {
        Mockito.when(userService.getAppUserForName(name)).thenReturn(null);

        assertThrows(AccessDeniedException.class, () -> {
            ownedTaskService.findById(1L, name);
        });
    }

    @Test
    public void saveTask_happypath() {
        Mockito.when(userService.getAppUserForName(name)).thenReturn(user);
        Task taskToBeUpdated = Task.builder().build();
        Date dueDate = new Date();
        Task task = Task.builder().name("my_task").description("my tas desc").doneCriteria("task done cri").dueDate(dueDate).status(Status.COMPLETED.toString()).build();
        Mockito.when(taskRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(taskToBeUpdated));
        Mockito.when(taskRepository.save(taskToBeUpdated)).thenReturn(taskToBeUpdated);

        Optional<Task> optUpdatedTask = ownedTaskService.save(1L, task, name);
        Task updatedTask = optUpdatedTask.get();


        assertEquals(Status.COMPLETED.toString(), updatedTask.getStatus());
        assertEquals("my tas desc", updatedTask.getDescription());
        assertEquals("task done cri", updatedTask.getDoneCriteria());
        assertEquals(dueDate, updatedTask.getDueDate());
    }

    @Test
    public void saveTaskTest_invalid_user() {
        Mockito.when(userService.getAppUserForName(name)).thenReturn(null);
        Date dueDate = new Date();
        Task task = Task.builder().name("my_task").description("my tas desc").doneCriteria("task done cri").dueDate(dueDate).status(Status.COMPLETED.toString()).build();

        assertThrows(AccessDeniedException.class, () -> {
            ownedTaskService.save(1L, task, name);
        });
    }

    @Test
    public void saveTaskTest_NullTask() {
        Mockito.when(userService.getAppUserForName(name)).thenReturn(user);
        Date dueDate = new Date();
        Task task = null;

        assertThrows(IllegalArgumentException.class, () -> {
            ownedTaskService.save(1L, task, name);
        });
    }

    @Test
    public void findAllTasks() {
        Task task_1 = Task.builder().build();
        Task task_2 = Task.builder().build();
        Task task_3 = Task.builder().build();
        List<Task> tasks = new ArrayList<>(Arrays.asList(task_1, task_2, task_3));
        Mockito.when(userService.getAppUserForName(name)).thenReturn(user);
        Mockito.when(taskRepository.findAllByOwner(user)).thenReturn(tasks);

        Iterable<Task> foundTasksItr = ownedTaskService.findAllTasks(name);
        List<Task> foundTasks = new ArrayList<>();
        foundTasksItr.forEach(foundTasks::add);

        assertEquals(3, foundTasks.size());
    }

    @Test
    public void findAllTasks_invalidUser() {
        Mockito.when(userService.getAppUserForName(name)).thenReturn(null);
        assertThrows(AccessDeniedException.class, () -> {
            ownedTaskService.findAllTasks(name);
        });
    }

}
