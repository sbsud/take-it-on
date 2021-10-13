package com.takeiton.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.takeiton.models.AppUser;
import com.takeiton.models.Milestone;
import com.takeiton.models.Objective;
import com.takeiton.services.AppUserDetailsService;
import com.takeiton.services.OwnedMilestoneService;
import com.takeiton.services.TokenService;
import com.takeiton.util.JWTUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MilestoneController.class)
public class MilestoneControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    OwnedMilestoneService milestoneService;

    @MockBean
    JWTUtil jwtUtil;

    @MockBean
    AppUserDetailsService userDetailsService;
    @MockBean
    TokenService tokenService;

    String baseUrl = "/api/milestone";

    AppUser appUser = AppUser.builder().username("user_1").password("").build();
    Milestone milestone_1 = Milestone.builder().id(1L).name("milestone_1").description("first").doneCriteria("dc1").status("NOT_STARTED").dueDate(new Date()).owner(appUser).build();
    //    Milestone milestone_1 = Milestone.builder().id(1L).name("milestone_1").description("first").doneCriteria("dc1").status("NOT_STARTED").dueDate(new Date()).owner(appUser).build();
    Milestone milestone_2 = Milestone.builder().id(1L).name("milestone_2").description("milestone_2").doneCriteria("dc1").status("NOT_STARTED").dueDate(new Date()).owner(appUser).build();
    Milestone milestone_3 = Milestone.builder().id(1L).name("milestone_3").description("milestone_3").doneCriteria("dc1").status("NOT_STARTED").dueDate(new Date()).owner(appUser).build();
    Objective OBJ_2 = Objective.builder().id(2L).description("second").doneCriteria("dc2").dueDate(new Date()).status("not started").owner(appUser).milestones(new ArrayList<>(Arrays.asList(milestone_1, milestone_2, milestone_3))).build();

    Objective OBJ_3 = Objective.builder().id(3L).description("third").doneCriteria("dc3").dueDate(new Date()).status("not started").owner(appUser).build();
    User userDetails = new User("user_1", "", Collections.emptyList());
    String token = "dummy_token";

    @BeforeEach
    private void createMocks() {
        Mockito.when(jwtUtil.extractUsername(token)).thenReturn("user_1");
        Mockito.when(userDetailsService.loadUserByUsername("user_1")).thenReturn(userDetails);
        Mockito.when(jwtUtil.validateToken(token, userDetails)).thenReturn(true);
        Mockito.when(tokenService.isActiveToken("user_1", token)).thenReturn(true);
    }

    @Test
    public void getMilestone_byId_success() throws Exception {
        Mockito.when(milestoneService.findById(milestone_1.getId(), "user_1")).thenReturn(Optional.of(milestone_1));

        mockMvc.perform(MockMvcRequestBuilders
                        .get(baseUrl + "/1/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.name", is("milestone_1")))
                .andExpect(jsonPath("$.status", is("NOT_STARTED")));
    }

    @Test
    public void getMilestone_for_objective() throws Exception {
        List<Milestone> milestones = new ArrayList<>(Arrays.asList(milestone_1, milestone_2, milestone_3));
        Mockito.when(milestoneService.findMilestonesForObjective(1L, "user_1")).thenReturn(milestones);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/objective" + "/1" + "/milestone")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    public void createMilestone_happyCase() throws Exception {
        Milestone milestone_create = Milestone.builder()
                .id(1L)
                .name("milestone_create")
                .description("milestone_create_desc")
                .doneCriteria("dc1")
                .dueDate(new Date())
//                .status("not started")
                .build();
        Mockito.when(milestoneService.createMilestone(1L, milestone_create, appUser.getUsername())).thenReturn(milestone_create);

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/api/objective" + "/1" + "/milestone")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(milestone_create));

        mockMvc.perform(mockRequest)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.name", is("milestone_create")))
                .andExpect(jsonPath("$.description", is("milestone_create_desc")));
    }

}
