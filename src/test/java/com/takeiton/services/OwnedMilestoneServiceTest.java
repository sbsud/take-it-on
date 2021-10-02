package com.takeiton.services;

import com.takeiton.models.AppUser;
import com.takeiton.models.Milestone;
import com.takeiton.models.Objective;
import com.takeiton.repositories.AppUserRepository;
import com.takeiton.repositories.MilestoneRepository;
import com.takeiton.repositories.ObjectiveRepository;
import com.takeiton.util.Status;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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


}
